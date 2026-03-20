"""
Layer 1: CPC (对比预测编码) 编码器 - 生理信号特化版本

功能:
- 使用 Transformer 提取时间序列的鲁棒表征
- 通过对比学习 (InfoNCE) 实现自监督训练
- 支持多尺度预测 (短期、中期、长期)
- 生理信号特化: 低频时序(1Hz)、缺失值处理、生理约束

架构:
Input (B, T, D) -> Physiological Constraint -> Positional Encoding -> Transformer Encoder -> z (B, T, hidden_dim)
                                                    -> Predictor -> Predictions (B, K, D)

改进:
1. 分层负采样: 同类用户作为hard negatives
2. 缺失值处理: 支持传感器故障/用户摘下设备的场景
3. 生理约束: 心率40-200bpm, 血氧80-100%
4. 低频适配: 针对1Hz生理信号优化位置编码
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
import math
from typing import Tuple, Dict, List, Optional
import sys
from pathlib import Path

# 导入生理信号特化工具
from ..utils.negative_sampling import HierarchicalNegativeSampler, AdaptiveNegativeSampler
from ..utils.missing_value_handler import (
    MissingValueHandler,
    PhysiologicalConstraintLayer,
    create_attention_mask_with_missing,
)


class PositionalEncoding(nn.Module):
    """
    位置编码模块 (sinusoidal) - 生理信号特化

    针对低频生理信号(1Hz)优化，调整位置编码周期
    """

    def __init__(
        self,
        d_model: int,
        max_len: int = 10000,
        dropout: float = 0.1,
        # 生理信号特化参数
        circadian_period: int = 86400,  # 24小时周期 (秒)
        sampling_rate: int = 1,  # 采样率 (Hz)
    ):
        """
        Args:
            d_model: 模型维度
            max_len: 最大序列长度
            dropout: Dropout 概率
            circadian_period: 昼夜节律周期 (秒)
            sampling_rate: 采样率 (Hz)
        """
        super().__init__()
        self.dropout = nn.Dropout(p=dropout)
        self.circadian_period = circadian_period
        self.sampling_rate = sampling_rate

        # 创建位置编码矩阵
        pe = torch.zeros(max_len, d_model)  # [max_len, d_model]

        # 计算位置编码
        position = torch.arange(0, max_len, dtype=torch.float32).unsqueeze(1)  # [max_len, 1]

        # 计算分母项: 10000^(2i/d_model)
        div_term = torch.exp(
            torch.arange(0, d_model, 2).float() * (-math.log(10000.0) / d_model)
        )  # [d_model/2]

        # 偶数维度使用 sin
        pe[:, 0::2] = torch.sin(position * div_term)  # [max_len, d_model/2]

        # 奇数维度使用 cos
        pe[:, 1::2] = torch.cos(position * div_term)  # [max_len, d_model/2]

        # 添加 batch 维度: [1, max_len, d_model]
        pe = pe.unsqueeze(0)

        # 注册为 buffer (不作为参数训练，但会被保存到 state_dict)
        self.register_buffer('pe', pe)

        # 创建昼夜节律编码 (24小时周期)
        circadian_len = circadian_period // sampling_rate  # 一天的采样点数
        circadian_pe = torch.zeros(circadian_len, d_model)
        circadian_pos = torch.arange(0, circadian_len, dtype=torch.float32).unsqueeze(1)

        # 使用更长的周期来编码昼夜节律
        circadian_div_term = torch.exp(
            torch.arange(0, d_model, 2).float() * (-math.log(10000.0) / d_model)
        )

        circadian_pe[:, 0::2] = torch.sin(circadian_pos * circadian_div_term)
        circadian_pe[:, 1::2] = torch.cos(circadian_pos * circadian_div_term)
        circadian_pe = circadian_pe.unsqueeze(0)

        self.register_buffer('circadian_pe', circadian_pe)

    def forward(self, x: torch.Tensor, timestamps: Optional[torch.Tensor] = None) -> torch.Tensor:
        """
        Args:
            x: 输入张量 [B, T, D]
            timestamps: 时间戳 (秒) [B, T]，用于添加昼夜节律编码

        Returns:
            添加位置编码后的张量 [B, T, D]
        """
        batch_size, seq_len, _ = x.shape

        # 基础位置编码
        x = x + self.pe[:, :seq_len, :]

        # 如果有时间戳，添加昼夜节律编码
        if timestamps is not None and hasattr(self, 'circadian_pe'):
            # 将时间戳转换为一天内的位置 (0-86399秒)
            day_positions = (timestamps % self.circadian_period).long()
            day_positions = torch.clamp(day_positions, 0, self.circadian_pe.size(1) - 1)

            # 收集昼夜节律编码
            # [B, T] -> [B, T, D]
            circadian_encoding = self.circadian_pe[0, day_positions, :]
            x = x + circadian_encoding

        return self.dropout(x)


class LearnablePositionalEncoding(nn.Module):
    """
    可学习的位置编码

    相比 sinusoidal 位置编码，这种方式可以学习数据相关的位置模式
    """

    def __init__(self, d_model: int, max_len: int = 10000, dropout: float = 0.1):
        """
        Args:
            d_model: 模型维度
            max_len: 最大序列长度
            dropout: Dropout 概率
        """
        super().__init__()
        self.dropout = nn.Dropout(p=dropout)

        # 可学习的位置嵌入
        self.pe = nn.Parameter(torch.randn(1, max_len, d_model) * 0.1)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Args:
            x: 输入张量 [B, T, D]

        Returns:
            添加位置编码后的张量 [B, T, D]
        """
        x = x + self.pe[:, :x.size(1), :]
        return self.dropout(x)


class TemporalAttentionPooling(nn.Module):
    """Learnable temporal pooling that replaces fixed mean pooling."""

    def __init__(self, hidden_dim: int, dropout: float = 0.1):
        super().__init__()
        self.score = nn.Sequential(
            nn.Linear(hidden_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, 1),
        )

    def forward(
        self,
        x: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        scores = self.score(x).squeeze(-1)

        if mask is not None:
            scores = scores.masked_fill(~mask.bool(), float("-inf"))

        weights = torch.softmax(scores, dim=-1)

        if mask is not None:
            weights = weights * mask.float()
            weights = weights / weights.sum(dim=-1, keepdim=True).clamp(min=1e-8)

        return torch.sum(x * weights.unsqueeze(-1), dim=1)


class TransformerEncoder(nn.Module):
    """
    Transformer 编码器 - 生理信号特化

    使用多头自注意力机制捕获时间序列的长程依赖
    支持缺失值掩码和生理约束
    """

    def __init__(
        self,
        input_dim: int,
        d_model: int = 128,
        nhead: int = 8,
        num_layers: int = 4,
        dim_feedforward: int = 512,
        dropout: float = 0.1,
        activation: str = "gelu",
        use_learnable_pe: bool = False,
        # 生理信号特化参数
        use_physiological_constraint: bool = True,
        use_missing_value_handler: bool = True,
    ):
        """
        Args:
            input_dim: 输入特征维度
            d_model: Transformer 隐藏维度
            nhead: 多头注意力的头数
            num_layers: Transformer 层数
            dim_feedforward: 前馈网络隐藏层维度
            dropout: Dropout 概率
            activation: 激活函数 ("relu" 或 "gelu")
            use_learnable_pe: 是否使用可学习的位置编码
            use_physiological_constraint: 是否使用生理约束
            use_missing_value_handler: 是否使用缺失值处理
        """
        super().__init__()

        self.input_dim = input_dim
        self.d_model = d_model
        self.nhead = nhead
        self.num_layers = num_layers

        # 生理约束层
        self.use_physiological_constraint = use_physiological_constraint
        if use_physiological_constraint:
            self.constraint_layer = PhysiologicalConstraintLayer(
                constraint_method="clip"
            )

        # 缺失值处理器
        self.use_missing_value_handler = use_missing_value_handler
        if use_missing_value_handler:
            self.missing_handler = MissingValueHandler(
                strategy="mask",
                hidden_dim=d_model,
            )

        # 输入投影层: 将输入维度映射到 d_model
        self.input_projection = nn.Linear(input_dim, d_model)

        # 位置编码
        if use_learnable_pe:
            self.pos_encoder = LearnablePositionalEncoding(d_model, dropout=dropout)
        else:
            self.pos_encoder = PositionalEncoding(d_model, dropout=dropout)

        # Transformer 编码器层
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=d_model,
            nhead=nhead,
            dim_feedforward=dim_feedforward,
            dropout=dropout,
            activation=activation,
            batch_first=True,  # 输入格式: [batch, seq, feature]
            norm_first=True,   # Pre-LN架构 (更稳定)
        )

        # 堆叠多层 Transformer
        self.transformer_encoder = nn.TransformerEncoder(
            encoder_layer,
            num_layers=num_layers,
        )

        # 层归一化 (用于稳定输出)
        self.output_norm = nn.LayerNorm(d_model)

    def forward(
        self,
        x: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
        src_key_padding_mask: Optional[torch.Tensor] = None,
        missing_mask: Optional[torch.Tensor] = None,
        timestamps: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        """
        前向传播

        Args:
            x: 输入张量 [B, T, D]
            mask: 自注意力掩码 [T, T]
            src_key_padding_mask: 填充掩码 [B, T]
            missing_mask: 缺失值掩码 [B, T, D], 1表示有数据，0表示缺失
            timestamps: 时间戳 [B, T] (秒)，用于添加昼夜节律编码

        Returns:
            编码后的表征 [B, T, d_model]
        """
        # 应用生理约束
        if self.use_physiological_constraint:
            x = self.constraint_layer(x)

        # 输入投影
        x = self.input_projection(x)  # [B, T, d_model]

        # 处理缺失值
        if self.use_missing_value_handler and missing_mask is not None:
            # 应用缺失值处理 (使用mask token填充)
            x, _ = self.missing_handler(x, mask=missing_mask, encoded=x)

            # 创建key padding mask: [B, T] - True表示需要忽略的位置
            # 如果某个时间步的所有特征都缺失，则忽略该位置
            if missing_mask.dim() == 3:
                # [B, T, D] -> [B, T]: 如果所有特征都缺失，则标记为padding
                src_key_padding_mask = (missing_mask.sum(dim=-1) == 0)
            else:
                src_key_padding_mask = (missing_mask == 0)

            # 如果提供了原有的padding mask，合并
            if src_key_padding_mask is not None and missing_mask is not None:
                # 已有的src_key_padding_mask是上面刚创建的，这里需要重新处理
                pass  # 已经处理过了

        # 添加位置编码 (支持昼夜节律)
        if isinstance(self.pos_encoder, PositionalEncoding):
            x = self.pos_encoder(x, timestamps=timestamps)
        else:
            x = self.pos_encoder(x)

        # Transformer 编码
        x = self.transformer_encoder(
            x,
            mask=mask,
            src_key_padding_mask=src_key_padding_mask,
        )

        # 输出归一化
        x = self.output_norm(x)

        return x


class CPCPredictor(nn.Module):
    """
    CPC 预测头

    从上下文表征预测未来的编码
    """

    def __init__(
        self,
        hidden_dim: int,
        prediction_dim: int,
        num_prediction_steps: int = 5,
    ):
        """
        Args:
            hidden_dim: 上下文表征维度
            prediction_dim: 预测维度 (通常等于输入维度或编码维度)
            num_prediction_steps: 预测的时间步数
        """
        super().__init__()

        self.hidden_dim = hidden_dim
        self.prediction_dim = prediction_dim
        self.num_prediction_steps = num_prediction_steps

        # 预测头: 将上下文聚合后预测未来
        self.predictor = nn.Sequential(
            nn.Linear(hidden_dim, hidden_dim),
            nn.GELU(),
            nn.Dropout(0.1),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(0.1),
            nn.Linear(hidden_dim // 2, prediction_dim),
        )

        # 多步预测的权重矩阵 (每个预测步独立变换)
        self.prediction_weights = nn.Parameter(
            torch.randn(num_prediction_steps, hidden_dim, prediction_dim) * 0.1
        )

    def forward(self, context: torch.Tensor, step_idx: int = 0) -> torch.Tensor:
        """
        预测未来编码

        Args:
            context: 上下文表征 [B, T_c, H] 或聚合后的上下文 [B, H]
            step_idx: 预测步索引 (0 到 num_prediction_steps-1)

        Returns:
            预测的编码 [B, prediction_dim]
        """
        if context.dim() == 3:  # [B, T_c, H]
            # 时间维度聚合 (均值池化)
            context = context.mean(dim=1)  # [B, H]

        # 使用对应步的预测权重
        weight = self.prediction_weights[step_idx]  # [H, prediction_dim]

        # 投影到预测空间
        prediction = torch.matmul(context, weight)  # [B, prediction_dim]

        return prediction

    def predict_all_steps(self, context: torch.Tensor) -> torch.Tensor:
        """
        预测所有时间步

        Args:
            context: 上下文表征 [B, H] 或 [B, T_c, H]

        Returns:
            所有步的预测 [B, num_steps, prediction_dim]
        """
        if context.dim() == 3:
            # [B, T_c, H] -> [B, H]
            context = context.mean(dim=1)

        batch_size = context.size(0)

        # 使用所有预测权重
        # context: [B, H], weights: [num_steps, H, pred_dim]
        # 需要对每个预测步分别计算
        predictions_list = []
        for step in range(self.num_prediction_steps):
            # 单步预测: [B, H] @ [H, pred_dim] -> [B, pred_dim]
            step_pred = torch.matmul(
                context,  # [B, H]
                self.prediction_weights[step]  # [H, pred_dim]
            )
            predictions_list.append(step_pred)

        # 堆叠所有步: [num_steps, B, pred_dim] -> [B, num_steps, pred_dim]
        predictions = torch.stack(predictions_list, dim=0).transpose(0, 1)

        return predictions


class InfoNCELoss(nn.Module):
    """
    InfoNCE (Information Noise Contrastive Estimation) 损失函数

    用于对比学习，区分正样本(真实未来)和负样本(错误未来)

    数学形式:
        L = -log[exp(sim(z_k, c)/τ) / Σ_j exp(sim(z_j, c)/τ)]

    其中:
        - z_k: 真实未来编码 (正样本)
        - z_j: 包括正样本和负样本的所有候选
        - c: 上下文表征
        - τ: 温度参数
    """

    def __init__(self, temperature: float = 0.07, reduction: str = "mean"):
        """
        Args:
            temperature: 温度参数 (越小，对比越明显)
            reduction: 约简方式 ("mean", "sum", "none")
        """
        super().__init__()
        self.temperature = temperature
        self.reduction = reduction

    def forward(
        self,
        context: torch.Tensor,
        positive: torch.Tensor,
        negatives: torch.Tensor,
    ) -> torch.Tensor:
        """
        计算 InfoNCE 损失

        Args:
            context: 上下文表征 [B, H] 或 [B, 1, H]
            positive: 正样本 (真实未来) [B, H] 或 [B, 1, H]
            negatives: 负样本 [B, K, H]，K 是负样本数

        Returns:
            损失值标量或张量
        """
        # 确保维度正确
        if context.dim() == 3:
            context = context.squeeze(1)  # [B, H]
        if positive.dim() == 3:
            positive = positive.squeeze(1)  # [B, H]

        # 计算正样本得分: [B]
        positive_score = torch.sum(context * positive, dim=-1) / self.temperature

        # 计算负样本得分: [B, K]
        negative_scores = torch.matmul(
            context.unsqueeze(1),  # [B, 1, H]
            negatives.transpose(1, 2)  # [B, H, K]
        ).squeeze(1) / self.temperature  # [B, K]

        # 拼接正负样本得分: [B, K+1]
        # 正样本放在第一个位置
        all_scores = torch.cat([positive_score.unsqueeze(1), negative_scores], dim=1)

        # 计算交叉熵损失
        # 目标是第一个类别 (正样本)
        labels = torch.zeros(context.size(0), dtype=torch.long, device=context.device)

        loss = F.cross_entropy(all_scores, labels, reduction=self.reduction)

        return loss


class CPCEncoder(nn.Module):
    """
    完整的 CPC (对比预测编码) 编码器 - 生理信号特化

    组合 Transformer 编码器和 CPC 预测头，实现端到端对比学习
    支持:
    - 分层负采样 (hard negatives)
    - 缺失值处理
    - 生理约束
    - 昼夜节律编码
    """

    def __init__(
        self,
        input_dim: int,
        d_model: int = 128,
        nhead: int = 8,
        num_layers: int = 4,
        dim_feedforward: int = 512,
        num_prediction_steps: int = 5,
        temperature: float = 0.07,
        dropout: float = 0.1,
        use_learnable_pe: bool = False,
        # 生理信号特化参数
        use_hierarchical_sampling: bool = True,
        num_negatives: int = 10,
        hard_negative_ratio: float = 0.5,
        use_physiological_constraint: bool = True,
        use_missing_value_handler: bool = True,
        context_pooling: str = "mean",
    ):
        """
        Args:
            input_dim: 输入特征维度 (如: 心率、血氧、加速度 = 3)
            d_model: Transformer 隐藏维度
            nhead: 多头注意力头数
            num_layers: Transformer 层数
            dim_feedforward: 前馈网络维度
            num_prediction_steps: 预测步数
            temperature: InfoNCE 温度参数
            dropout: Dropout 概率
            use_learnable_pe: 是否使用可学习位置编码
            use_hierarchical_sampling: 是否使用分层负采样
            num_negatives: 负样本数量
            hard_negative_ratio: hard negatives 占比
            use_physiological_constraint: 是否使用生理约束
            use_missing_value_handler: 是否使用缺失值处理
        """
        super().__init__()

        self.input_dim = input_dim
        self.d_model = d_model
        self.num_prediction_steps = num_prediction_steps
        self.use_hierarchical_sampling = use_hierarchical_sampling
        self.context_pooling = context_pooling

        # Transformer 编码器
        self.encoder = TransformerEncoder(
            input_dim=input_dim,
            d_model=d_model,
            nhead=nhead,
            num_layers=num_layers,
            dim_feedforward=dim_feedforward,
            dropout=dropout,
            use_learnable_pe=use_learnable_pe,
            use_physiological_constraint=use_physiological_constraint,
            use_missing_value_handler=use_missing_value_handler,
        )

        # AR (Autoregressive) 模块: 将上下文编码聚合为固定维度
        self.ar_model = nn.Sequential(
            nn.Linear(d_model, d_model),
            nn.GELU(),
            nn.Dropout(dropout),
        )

        if context_pooling == "attention":
            self.context_pooler = TemporalAttentionPooling(d_model, dropout=dropout)
        elif context_pooling == "mean":
            self.context_pooler = None
        else:
            raise ValueError(f"Unknown context_pooling: {context_pooling}")

        # 预测头
        self.predictor = CPCPredictor(
            hidden_dim=d_model,
            prediction_dim=d_model,
            num_prediction_steps=num_prediction_steps,
        )

        # 损失函数
        self.criterion = InfoNCELoss(temperature=temperature)

        # 负采样器
        if use_hierarchical_sampling:
            self.negative_sampler = HierarchicalNegativeSampler(
                num_negatives=num_negatives,
                hard_negative_ratio=hard_negative_ratio,
            )
        else:
            self.negative_sampler = NegativeSampler(
                num_negatives=num_negatives,
                sampling_strategy="batch",
            )

    def encode(
        self,
        x: torch.Tensor,
        missing_mask: Optional[torch.Tensor] = None,
        timestamps: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        """
        编码输入序列

        Args:
            x: 输入张量 [B, T, D]
            missing_mask: 缺失值掩码 [B, T, D]
            timestamps: 时间戳 [B, T] (秒)

        Returns:
            编码表征 [B, T, d_model]
        """
        return self.encoder(x, missing_mask=missing_mask, timestamps=timestamps)

    def extract_context(
        self,
        encoded: torch.Tensor,
        context_length: Optional[int] = None,
    ) -> torch.Tensor:
        """
        从编码中提取上下文

        Args:
            encoded: 编码后的表征 [B, T, H]
            context_length: 上下文长度 (None 表示使用全部)

        Returns:
            上下文表征 [B, H]
        """
        if context_length is not None:
            # 只使用前 context_length 个时间步
            context = encoded[:, :context_length, :]
        else:
            # 使用全部时间步
            context = encoded

        # 时间维度聚合
        context = context.mean(dim=1)  # [B, H]

        # 通过 AR 模型
        context = self.ar_model(context)  # [B, H]

        return context

    def extract_context_attention(
        self,
        encoded: torch.Tensor,
        context_length: Optional[int] = None,
        mask: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        if context_length is not None:
            context = encoded[:, :context_length, :]
            context_mask = mask[:, :context_length] if mask is not None else None
        else:
            context = encoded
            context_mask = mask

        if self.context_pooling == "attention":
            context = self.context_pooler(context, mask=context_mask)
        elif context_mask is not None:
            weights = context_mask.float()
            context = (context * weights.unsqueeze(-1)).sum(dim=1)
            context = context / weights.sum(dim=1, keepdim=True).clamp(min=1e-8)
        else:
            context = context.mean(dim=1)

        return self.ar_model(context)

    def predict(
        self,
        context: torch.Tensor,
        step_idx: Optional[int] = None,
    ) -> torch.Tensor:
        """
        从上下文预测未来编码

        Args:
            context: 上下文表征 [B, H]
            step_idx: 预测步索引 (None 表示预测所有步)

        Returns:
            预测编码 [B, H] 或 [B, num_steps, H]
        """
        if step_idx is None:
            return self.predictor.predict_all_steps(context.unsqueeze(1))
        else:
            return self.predictor(context.unsqueeze(1), step_idx)

    def forward(
        self,
        x: torch.Tensor,
        context_length: Optional[int] = None,
        return_encoded: bool = False,
        missing_mask: Optional[torch.Tensor] = None,
        timestamps: Optional[torch.Tensor] = None,
    ) -> Dict[str, torch.Tensor]:
        """
        前向传播

        Args:
            x: 输入张量 [B, T, D]
            context_length: 上下文长度
            return_encoded: 是否返回完整编码
            missing_mask: 缺失值掩码 [B, T, D]
            timestamps: 时间戳 [B, T] (秒)，用于添加昼夜节律编码

        Returns:
            包含以下键的字典:
            - "context": 上下文表征 [B, H]
            - "predictions": 所有预测 [B, num_steps, H]
            - "encoded": 完整编码 [B, T, H] (如果 return_encoded=True)
        """
        # 编码输入
        encoded = self.encode(x, missing_mask=missing_mask, timestamps=timestamps)  # [B, T, H]

        # 提取上下文
        context = self.extract_context_attention(encoded, context_length)  # [B, H]

        # 预测未来
        predictions = self.predict(context)  # [B, num_steps, H]

        output = {
            "context": context,
            "predictions": predictions,
        }

        if return_encoded:
            output["encoded"] = encoded

        return output

    def compute_loss(
        self,
        x: torch.Tensor,
        negatives: Optional[torch.Tensor] = None,
        context_ratio: float = 0.5,
        user_clusters: Optional[torch.Tensor] = None,
        timestamps: Optional[torch.Tensor] = None,
        missing_mask: Optional[torch.Tensor] = None,
    ) -> Tuple[torch.Tensor, Dict[str, float]]:
        """
        计算 CPC 损失 - 支持分层负采样

        Args:
            x: 输入序列 [B, T, D]
            negatives: 负样本 [B, K, D] 或 [B, K, H] (可选，如果None则自动采样)
            context_ratio: 上下文长度占比
            user_clusters: 用户聚类标签 [B] (用于分层负采样)
            timestamps: 时间戳 [B, T] (用于分层负采样和昼夜节律)
            missing_mask: 缺失值掩码 [B, T, D]

        Returns:
            (总损失, 损失详情字典)
        """
        batch_size, seq_len, _ = x.shape
        context_length = int(seq_len * context_ratio)

        # 前向传播
        output = self.forward(
            x,
            context_length=context_length,
            return_encoded=True,
            missing_mask=missing_mask,
            timestamps=timestamps,
        )

        context = output["context"]  # [B, H]
        predictions = output["predictions"]  # [B, num_steps, H]
        encoded = output["encoded"]  # [B, T, H]

        # 计算多尺度损失
        losses = []
        num_steps = min(self.num_prediction_steps, seq_len - context_length)

        # 准备目标索引
        target_indices = torch.arange(
            context_length,
            context_length + num_steps,
            device=x.device,
        ).unsqueeze(0).expand(batch_size, -1)  # [B, num_steps]

        # 如果没有提供负样本，使用负采样器
        if negatives is None:
            negatives = self.negative_sampler.sample(
                encoded,
                target_indices,
                user_clusters=user_clusters,
                timestamps=timestamps,
            )  # [B, num_steps, K, H]

        for step in range(num_steps):
            # 正样本: 真实未来的编码
            target_idx = context_length + step
            if target_idx < seq_len:
                positive = encoded[:, target_idx, :]  # [B, H]

                # 获取该步的负样本
                # 处理两种情况: 4维 [B, num_steps, K, H] 或 3维 [B, K, H]
                if negatives.dim() == 4:
                    step_negatives = negatives[:, step, :, :]  # [B, K, H]
                else:
                    # 如果负样本是3维，重复使用于所有步
                    step_negatives = negatives  # [B, K, H]

                # 计算该步的损失
                step_loss = self.criterion(
                    context,
                    positive,
                    step_negatives,
                )
                losses.append(step_loss)

        # 总损失 (所有步的平均)
        total_loss = torch.stack(losses).mean()

        # 损失详情
        loss_details = {
            "total_loss": total_loss.item(),
            "num_steps": num_steps,
        }

        # 如果使用分层负采样，添加hard negative比例信息
        if self.use_hierarchical_sampling:
            loss_details["hard_negative_ratio"] = self.negative_sampler.hard_negative_ratio

        return total_loss, loss_details


class NegativeSampler:
    """
    负样本采样器

    从同一批或其他批中采样负样本，用于对比学习
    """

    def __init__(self, num_negatives: int = 10, sampling_strategy: str = "batch"):
        """
        Args:
            num_negatives: 负样本数量
            sampling_strategy: 采样策略 ("batch", "random", "future")
        """
        self.num_negatives = num_negatives
        self.sampling_strategy = sampling_strategy

    def sample(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
    ) -> torch.Tensor:
        """
        采样负样本

        Args:
            encoded: 编码后的序列 [B, T, H]
            target_indices: 目标时间步索引 [B, num_steps]

        Returns:
            负样本 [B, num_steps, K, H]，K 是负样本数
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        num_steps = target_indices.size(1)

        if self.sampling_strategy == "batch":
            # 从同一批的其他样本中采样
            # 这里的策略是: 使用其他样本的同一位置

            negatives_list = []

            for step in range(num_steps):
                step_negatives = []

                for k in range(self.num_negatives):
                    # 随机选择一个不同的样本
                    neg_indices = torch.randint(0, batch_size, (batch_size,), device=encoded.device)

                    # 获取该样本的目标位置编码
                    target_pos = target_indices[:, step]  # [B]
                    neg_sample = encoded[neg_indices, target_pos, :]  # [B, H]

                    step_negatives.append(neg_sample)

                # 堆叠负样本: [B, K, H]
                step_negatives = torch.stack(step_negatives, dim=1)
                negatives_list.append(step_negatives)

            # 堆叠所有步: [B, num_steps, K, H]
            negatives = torch.stack(negatives_list, dim=1)

        elif self.sampling_strategy == "random":
            # 随机采样编码中的任意位置
            neg_indices = torch.randint(
                0, seq_len,
                (batch_size, num_steps, self.num_negatives),
                device=encoded.device,
            )

            # 收集负样本
            negatives = encoded[
                torch.arange(batch_size).view(-1, 1, 1),  # [B, 1, 1]
                neg_indices,  # [B, num_steps, K]
            ]  # [B, num_steps, K, H]

        else:
            raise ValueError(f"Unknown sampling strategy: {self.sampling_strategy}")

        return negatives


def create_cpc_encoder(
    input_dim: int = 3,
    d_model: int = 128,
    nhead: int = 8,
    num_layers: int = 4,
    num_prediction_steps: int = 5,
    temperature: float = 0.07,
    # 生理信号特化参数
    use_hierarchical_sampling: bool = True,
    num_negatives: int = 10,
    hard_negative_ratio: float = 0.5,
    use_physiological_constraint: bool = True,
    use_missing_value_handler: bool = True,
    **kwargs,
) -> CPCEncoder:
    """
    创建 CPC 编码器的工厂函数 - 生理信号特化版本

    Args:
        input_dim: 输入维度
        d_model: 隐藏维度
        nhead: 注意力头数
        num_layers: 层数
        num_prediction_steps: 预测步数
        temperature: 温度参数
        use_hierarchical_sampling: 是否使用分层负采样
        num_negatives: 负样本数量
        hard_negative_ratio: hard negatives 占比
        use_physiological_constraint: 是否使用生理约束
        use_missing_value_handler: 是否使用缺失值处理
        **kwargs: 其他参数

    Returns:
        CPCEncoder 实例 (生理信号特化)
    """
    return CPCEncoder(
        input_dim=input_dim,
        d_model=d_model,
        nhead=nhead,
        num_layers=num_layers,
        num_prediction_steps=num_prediction_steps,
        temperature=temperature,
        use_hierarchical_sampling=use_hierarchical_sampling,
        num_negatives=num_negatives,
        hard_negative_ratio=hard_negative_ratio,
        use_physiological_constraint=use_physiological_constraint,
        use_missing_value_handler=use_missing_value_handler,
        **kwargs,
    )

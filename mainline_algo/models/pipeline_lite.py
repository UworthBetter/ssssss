"""
简化版Pipeline - 专为比赛优化

核心改动:
1. 去掉CPC对比学习（减少训练复杂度）
2. 去掉分层负采样（需要聚类信息）
3. 去掉缺失值处理器（演示数据无缺失）
4. 保留Evidential不确定性估计（核心亮点）
5. 添加时序池化（支持变长输入）

参数量: ~50K（原版~200K）
代码量: ~150行（原版~1000行）
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Dict, Optional


class SinusoidalPositionalEncoding(nn.Module):
    """简洁的位置编码，支持昼夜节律"""

    def __init__(self, d_model: int, max_len: int = 86400, dropout: float = 0.1):
        super().__init__()
        self.dropout = nn.Dropout(p=dropout)

        # 标准位置编码
        pe = torch.zeros(max_len, d_model)
        position = torch.arange(0, max_len).unsqueeze(1).float()
        div_term = torch.exp(
            torch.arange(0, d_model, 2).float()
            * (-torch.log(torch.tensor(10000.0)) / d_model)
        )
        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)
        self.register_buffer("pe", pe.unsqueeze(0))

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = x + self.pe[:, : x.size(1)]
        return self.dropout(x)


class TemporalAttentionPooling(nn.Module):
    """可学习的时序池化，替代简单均值池化"""

    def __init__(self, hidden_dim: int):
        super().__init__()
        self.score = nn.Sequential(
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.Tanh(),
            nn.Linear(hidden_dim // 2, 1),
        )

    def forward(
        self, x: torch.Tensor, mask: Optional[torch.Tensor] = None
    ) -> torch.Tensor:
        scores = self.score(x).squeeze(-1)  # [B, T]
        if mask is not None:
            scores = scores.masked_fill(~mask.bool(), float("-inf"))
        weights = F.softmax(scores, dim=-1)
        return (x * weights.unsqueeze(-1)).sum(dim=1)


class EvidentialHead(nn.Module):
    """Evidential输出头 - 核心亮点"""

    def __init__(self, input_dim: int, hidden_dim: int = 64):
        super().__init__()

        # 共享特征提取
        self.shared = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(0.1),
        )

        # 四个独立参数头
        self.gamma_head = nn.Linear(hidden_dim, 1)  # 预测均值（无约束）
        self.nu_head = nn.Sequential(
            nn.Linear(hidden_dim, 1), nn.Softplus()
        )  # 自由度 > 0
        self.alpha_head = nn.Sequential(
            nn.Linear(hidden_dim, 1), nn.Softplus()
        )  # 形状参数 > 0
        self.beta_head = nn.Sequential(
            nn.Linear(hidden_dim, 1), nn.Softplus()
        )  # 尺度参数 > 0

    def forward(self, x: torch.Tensor) -> Dict[str, torch.Tensor]:
        h = self.shared(x)

        gamma = self.gamma_head(h)
        nu = self.nu_head(h) + 0.1  # 确保 > 0.1
        alpha = self.alpha_head(h) + 1.01  # 确保 > 1.01
        beta = self.beta_head(h) + 1e-4  # 确保 > 0

        # 不确定性分解
        epistemic = beta / (nu * (alpha - 1) + 1e-8)  # 认知不确定性
        aleatoric = beta / (alpha - 1 + 1e-8)  # 偶然不确定性

        return {
            "gamma": gamma,
            "nu": nu,
            "alpha": alpha,
            "beta": beta,
            "epistemic": epistemic,
            "aleatoric": aleatoric,
            "total_uncertainty": epistemic + aleatoric,
        }


class LitePipeline(nn.Module):
    """
    精简版房颤风险筛查Pipeline

    架构: 输入 → 约束 → Transformer → 池化 → Evidential头 → 输出
    """

    def __init__(
        self,
        input_dim: int = 3,
        hidden_dim: int = 64,
        num_heads: int = 4,
        num_layers: int = 2,
        dropout: float = 0.1,
    ):
        super().__init__()

        # 输入投影
        self.input_proj = nn.Linear(input_dim, hidden_dim)

        # 位置编码
        self.pos_enc = SinusoidalPositionalEncoding(hidden_dim, dropout=dropout)

        # Transformer编码器
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=hidden_dim,
            nhead=num_heads,
            dim_feedforward=hidden_dim * 4,
            dropout=dropout,
            activation="gelu",
            batch_first=True,
            norm_first=True,  # Pre-LN更稳定
        )
        self.encoder = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)

        # 时序池化
        self.pooling = TemporalAttentionPooling(hidden_dim)

        # Evidential输出头
        self.evidential_head = EvidentialHead(hidden_dim, hidden_dim)

        self._init_weights()

    def _init_weights(self):
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.xavier_uniform_(m.weight)
                if m.bias is not None:
                    nn.init.zeros_(m.bias)

    def forward(
        self, x: torch.Tensor, mask: Optional[torch.Tensor] = None
    ) -> Dict[str, torch.Tensor]:
        """
        Args:
            x: [B, T, D] 输入序列
            mask: [B, T] 可选的掩码

        Returns:
            包含预测值和不确定性的字典
        """
        # 输入投影
        h = self.input_proj(x)  # [B, T, H]

        # 添加位置编码
        h = self.pos_enc(h)

        # Transformer编码
        h = self.encoder(h)  # [B, T, H]

        # 时序池化
        pooled = self.pooling(h, mask)  # [B, H]

        # Evidential预测
        output = self.evidential_head(pooled)

        # 添加编码表示（用于可视化）
        output["encoded"] = pooled

        return output


class EvidentialLoss(nn.Module):
    """Evidential损失函数 - NLL + 正则化"""

    def __init__(self, lambda_reg: float = 0.01):
        super().__init__()
        self.lambda_reg = lambda_reg

    def forward(self, gamma, nu, alpha, beta, y):
        """
        计算Evidential损失

        Args:
            gamma, nu, alpha, beta: NIG分布参数 [B, 1]
            y: 目标值 [B, 1]
        """
        # NLL损失（基于Student-t分布）
        error = y - gamma
        nll = (
            torch.lgamma(alpha + 0.5)
            - torch.lgamma(alpha)
            + 0.5 * torch.log(torch.tensor(3.14159 / nu))
            - alpha * torch.log(2 * beta + nu * error**2)
            + (alpha + 0.5) * torch.log(nu * error**2 + 2 * beta)
        )

        # 正则化损失（惩罚预测错误时的低不确定性）
        reg = torch.abs(error) * (2 * nu + alpha)

        loss = nll.mean() + self.lambda_reg * reg.mean()

        return {
            "loss": loss,
            "nll": nll.mean().item(),
            "reg": reg.mean().item(),
        }

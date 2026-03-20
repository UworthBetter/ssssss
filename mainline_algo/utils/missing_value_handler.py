"""
缺失值处理模块

针对生理信号数据中的缺失值问题:
1. 传感器故障导致的缺失
2. 用户摘下设备导致的缺失
3. 信号质量差导致的缺失

处理策略:
- 创建缺失值掩码
- 在Transformer中使用注意力掩码
- 插值填充
- 学习缺失模式
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Optional, Tuple, Dict
import numpy as np


class MissingValueHandler(nn.Module):
    """
    缺失值处理器

    支持多种缺失值处理策略
    """

    def __init__(
        self,
        strategy: str = "mask",
        fill_value: float = 0.0,
        learnable_mask_token: bool = True,
        hidden_dim: int = 128,
    ):
        """
        Args:
            strategy: 处理策略 ("mask", "interpolate", "forward_fill", "learnable")
            fill_value: 填充值 (用于非学习策略)
            learnable_mask_token: 是否使用可学习的mask token
            hidden_dim: 隐藏维度 (用于学习策略)
        """
        super().__init__()

        self.strategy = strategy
        self.fill_value = fill_value

        if learnable_mask_token:
            # 可学习的mask token
            self.mask_token = nn.Parameter(torch.randn(1, 1, hidden_dim) * 0.02)
        else:
            self.register_buffer("mask_token", torch.zeros(1, 1, hidden_dim))

        # 缺失值检测网络 (可选)
        self.missing_detector = nn.Sequential(
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(0.1),
            nn.Linear(hidden_dim // 2, 1),
            nn.Sigmoid(),
        )

    def create_missing_mask(
        self,
        data: torch.Tensor,
        missing_rate: float = 0.1,
        pattern: str = "random",
    ) -> torch.Tensor:
        """
        创建缺失值掩码

        Args:
            data: 输入数据 [B, T, D]
            missing_rate: 缺失率
            pattern: 缺失模式 ("random", "block", "periodic")

        Returns:
            mask: [B, T, D], 1表示有数据，0表示缺失
        """
        batch_size, seq_len, feat_dim = data.shape
        device = data.device

        if pattern == "random":
            # 随机缺失
            mask = torch.rand(batch_size, seq_len, feat_dim, device=device) > missing_rate

        elif pattern == "block":
            # 连续块缺失 (模拟传感器故障)
            mask = torch.ones(batch_size, seq_len, feat_dim, device=device)

            for b in range(batch_size):
                for d in range(feat_dim):
                    if torch.rand(1).item() < missing_rate:
                        # 随机选择缺失块的起始和长度
                        block_start = torch.randint(0, seq_len, (1,)).item()
                        block_len = torch.randint(1, seq_len // 4, (1,)).item()
                        block_end = min(block_start + block_len, seq_len)
                        mask[b, block_start:block_end, d] = 0

        elif pattern == "periodic":
            # 周期性缺失 (模拟用户摘下设备)
            mask = torch.ones(batch_size, seq_len, feat_dim, device=device)

            for b in range(batch_size):
                # 模拟夜间摘下设备
                night_start = seq_len // 3
                night_end = 2 * seq_len // 3
                mask[b, night_start:night_end, :] = 0

        else:
            raise ValueError(f"Unknown missing pattern: {pattern}")

        return mask.float()

    def forward(
        self,
        data: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
        encoded: Optional[torch.Tensor] = None,
    ) -> Tuple[torch.Tensor, torch.Tensor]:
        """
        处理缺失值

        Args:
            data: 输入数据 [B, T, D]
            mask: 缺失值掩码 [B, T, D], 1表示有数据，0表示缺失
            encoded: 编码后的数据 [B, T, H] (用于学习策略)

        Returns:
            (处理后的数据, 更新后的掩码)
        """
        if mask is None:
            # 如果没有提供掩码，假设没有缺失
            mask = torch.ones_like(data)

        if self.strategy == "mask":
            # 使用mask token填充
            if encoded is not None:
                # 使用可学习的mask token
                filled_data = self._apply_mask_token(encoded, mask)
            else:
                # 使用固定值填充
                filled_data = data * mask + self.fill_value * (1 - mask)

        elif self.strategy == "interpolate":
            # 线性插值
            filled_data = self._interpolate(data, mask)

        elif self.strategy == "forward_fill":
            # 前向填充
            filled_data = self._forward_fill(data, mask)

        elif self.strategy == "learnable":
            # 学习缺失模式
            filled_data = self._learnable_fill(data, mask, encoded)

        else:
            raise ValueError(f"Unknown strategy: {self.strategy}")

        return filled_data, mask

    def _apply_mask_token(
        self,
        encoded: torch.Tensor,
        mask: torch.Tensor,
    ) -> torch.Tensor:
        """
        使用mask token填充缺失位置
        """
        # mask: [B, T, D], 需要扩展到 [B, T, H]
        batch_size, seq_len, hidden_dim = encoded.shape

        # 如果mask是特征级别的，需要聚合
        if mask.dim() == 3 and mask.size(-1) != hidden_dim:
            # 聚合到序列级别: [B, T, D] -> [B, T, 1]
            mask_seq = (mask.sum(dim=-1, keepdim=True) > 0).float()
            # 扩展到隐藏维度: [B, T, 1] -> [B, T, H]
            mask_expanded = mask_seq.expand(-1, -1, hidden_dim)
        else:
            mask_expanded = mask

        # 应用mask token
        mask_token_expanded = self.mask_token.expand(batch_size, seq_len, -1)
        filled = encoded * mask_expanded + mask_token_expanded * (1 - mask_expanded)

        return filled

    def _interpolate(
        self,
        data: torch.Tensor,
        mask: torch.Tensor,
    ) -> torch.Tensor:
        """
        线性插值填充缺失值
        """
        batch_size, seq_len, feat_dim = data.shape
        device = data.device

        filled_data = data.clone()

        for b in range(batch_size):
            for d in range(feat_dim):
                valid_mask = mask[b, :, d].cpu().numpy()
                values = data[b, :, d].cpu().numpy()

                # 找到缺失位置
                missing_indices = np.where(valid_mask == 0)[0]

                if len(missing_indices) == 0:
                    continue

                # 线性插值
                valid_indices = np.where(valid_mask == 1)[0]

                if len(valid_indices) == 0:
                    # 如果全部缺失，使用均值填充
                    filled_data[b, :, d] = self.fill_value
                    continue

                # 插值
                interpolated = np.interp(
                    np.arange(seq_len),
                    valid_indices,
                    values[valid_indices],
                )

                filled_data[b, :, d] = torch.tensor(interpolated, device=device, dtype=data.dtype)

        return filled_data

    def _forward_fill(
        self,
        data: torch.Tensor,
        mask: torch.Tensor,
    ) -> torch.Tensor:
        """
        前向填充 (使用上一个有效值)
        """
        batch_size, seq_len, feat_dim = data.shape
        device = data.device

        filled_data = data.clone()

        for b in range(batch_size):
            for d in range(feat_dim):
                last_valid = self.fill_value
                for t in range(seq_len):
                    if mask[b, t, d] > 0:
                        last_valid = data[b, t, d]
                    else:
                        filled_data[b, t, d] = last_valid

        return filled_data

    def _learnable_fill(
        self,
        data: torch.Tensor,
        mask: torch.Tensor,
        encoded: Optional[torch.Tensor],
    ) -> torch.Tensor:
        """
        使用学习网络填充缺失值
        """
        if encoded is None:
            # 如果没有编码，使用简单填充
            return data * mask + self.fill_value * (1 - mask)

        # 检测缺失概率
        missing_prob = self.missing_detector(encoded)  # [B, T, 1]

        # 根据概率加权融合
        mask_token_expanded = self.mask_token.expand(encoded.size(0), encoded.size(1), -1)

        # 加权: 高缺失概率 -> 更多使用mask token
        weight = missing_prob * (1 - mask[:, :, :1])  # [B, T, 1]
        filled = encoded * (1 - weight) + mask_token_expanded * weight

        return filled


def create_padding_mask(
    seq_len: int,
    actual_lengths: torch.Tensor,
    device: torch.device,
) -> torch.Tensor:
    """
    创建填充掩码 (用于变长序列)

    Args:
        seq_len: 序列长度
        actual_lengths: 实际长度 [B]
        device: 设备

    Returns:
        padding_mask: [B, T], True表示填充位置
    """
    batch_size = actual_lengths.size(0)

    # 创建位置索引 [B, T]
    positions = torch.arange(seq_len, device=device).unsqueeze(0).expand(batch_size, -1)

    # 填充掩码: 位置 >= 实际长度 的位置为True (需要忽略)
    padding_mask = positions >= actual_lengths.unsqueeze(1)

    return padding_mask


def create_attention_mask_with_missing(
    missing_mask: torch.Tensor,
    padding_mask: Optional[torch.Tensor] = None,
) -> torch.Tensor:
    """
    创建考虑缺失值的注意力掩码

    Args:
        missing_mask: 缺失值掩码 [B, T, D] 或 [B, T]
        padding_mask: 填充掩码 [B, T]

    Returns:
        attention_mask: [B, T, T] 或 [B, 1, T, T]
    """
    batch_size, seq_len = missing_mask.size(0), missing_mask.size(1)
    device = missing_mask.device

    # 聚合特征维度: [B, T, D] -> [B, T]
    if missing_mask.dim() == 3:
        missing_mask_seq = (missing_mask.sum(dim=-1) > 0).float()
    else:
        missing_mask_seq = missing_mask.float()

    # 创建注意力掩码
    # 1表示有效，0表示需要mask
    attention_mask = missing_mask_seq.unsqueeze(1) * missing_mask_seq.unsqueeze(2)
    # [B, 1, T] * [B, T, 1] -> [B, T, T]

    # 如果有填充掩码，合并
    if padding_mask is not None:
        # padding_mask: True表示填充 -> 转换为0表示有效
        valid_mask = (~padding_mask).float()
        valid_mask = valid_mask.unsqueeze(1) * valid_mask.unsqueeze(2)
        attention_mask = attention_mask * valid_mask

    # 转换为Transformer需要的格式 (True表示忽略)
    attention_mask = attention_mask == 0

    return attention_mask


class PhysiologicalConstraintLayer(nn.Module):
    """
    生理约束层

    对生理信号施加合理的范围约束:
    - 心率范围: 40-200 bpm
    - 血氧范围: 80-100%
    - 加速度: 根据传感器范围
    """

    # 生理信号约束 (特征索引 -> (最小值, 最大值))
    DEFAULT_CONSTRAINTS = {
        0: (40.0, 200.0),   # 心率 (bpm)
        1: (80.0, 100.0),   # 血氧 (%)
        2: (-16.0, 16.0),   # 加速度 (g)
    }

    def __init__(
        self,
        constraints: Optional[Dict[int, Tuple[float, float]]] = None,
        constraint_method: str = "clip",
    ):
        """
        Args:
            constraints: 约束字典 {特征索引: (最小值, 最大值)}
            constraint_method: 约束方法 ("clip", "sigmoid", "tanh")
        """
        super().__init__()

        self.constraints = constraints or self.DEFAULT_CONSTRAINTS
        self.constraint_method = constraint_method

        # 为每个约束特征创建可学习的缩放参数
        self.scale_params = nn.ParameterDict()
        self.shift_params = nn.ParameterDict()

        for feat_idx, (min_val, max_val) in self.constraints.items():
            idx_str = str(feat_idx)
            self.scale_params[idx_str] = nn.Parameter(torch.tensor(1.0))
            self.shift_params[idx_str] = nn.Parameter(torch.tensor(0.0))

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        应用生理约束

        Args:
            x: 输入数据 [B, T, D]

        Returns:
            约束后的数据 [B, T, D]
        """
        constrained = x.clone()

        for feat_idx, (min_val, max_val) in self.constraints.items():
            idx_str = str(feat_idx)

            if feat_idx >= x.size(-1):
                continue

            # 获取该特征
            feat = x[..., feat_idx]

            # 应用约束
            if self.constraint_method == "clip":
                # 硬裁剪
                constrained[..., feat_idx] = torch.clamp(feat, min_val, max_val)

            elif self.constraint_method == "sigmoid":
                # 使用sigmoid映射到范围
                scale = self.scale_params[idx_str]
                shift = self.shift_params[idx_str]
                normalized = torch.sigmoid(feat * scale + shift)
                constrained[..., feat_idx] = min_val + normalized * (max_val - min_val)

            elif self.constraint_method == "tanh":
                # 使用tanh映射到范围
                scale = self.scale_params[idx_str]
                shift = self.shift_params[idx_str]
                normalized = torch.tanh(feat * scale + shift)
                constrained[..., feat_idx] = (min_val + max_val) / 2 + normalized * (max_val - min_val) / 2

        return constrained

    def detect_anomalies(
        self,
        x: torch.Tensor,
        return_mask: bool = False,
    ) -> Tuple[torch.Tensor, Optional[torch.Tensor]]:
        """
        检测异常值

        Args:
            x: 输入数据 [B, T, D]
            return_mask: 是否返回异常掩码

        Returns:
            (异常值数量, 异常掩码[可选])
        """
        anomaly_mask = torch.zeros_like(x, dtype=torch.bool)

        for feat_idx, (min_val, max_val) in self.constraints.items():
            if feat_idx >= x.size(-1):
                continue

            feat = x[..., feat_idx]
            feat_anomaly = (feat < min_val) | (feat > max_val)
            anomaly_mask[..., feat_idx] = feat_anomaly

        anomaly_count = anomaly_mask.sum()

        if return_mask:
            return anomaly_count, anomaly_mask
        return anomaly_count


def apply_physiological_constraints(
    data: torch.Tensor,
    feature_names: Optional[list] = None,
) -> torch.Tensor:
    """
    便捷函数: 应用生理约束

    Args:
        data: 输入数据 [B, T, D]
        feature_names: 特征名称列表 ["heart_rate", "spo2", "acceleration"]

    Returns:
        约束后的数据 [B, T, D]
    """
    if feature_names is None:
        feature_names = ["heart_rate", "spo2", "acceleration"]

    constraints = {}
    for i, name in enumerate(feature_names):
        if name == "heart_rate":
            constraints[i] = (40.0, 200.0)
        elif name == "spo2":
            constraints[i] = (80.0, 100.0)
        elif name == "acceleration":
            constraints[i] = (-16.0, 16.0)

    layer = PhysiologicalConstraintLayer(constraints, constraint_method="clip")
    return layer(data)

"""
合成生理信号数据集 - 用于测试模型

生成模拟的连续监测数据（心率、血氧、加速度）和房颤风险标签
"""

import torch
from torch.utils.data import Dataset, DataLoader
from dataclasses import dataclass
from typing import Tuple, Optional
import numpy as np


@dataclass
class TargetStats:
    """目标值的统计信息，用于归一化"""

    mean: float = 0.0
    std: float = 1.0


def normalize_targets(y: torch.Tensor, stats: TargetStats) -> torch.Tensor:
    """归一化目标值"""
    return (y - stats.mean) / (stats.std + 1e-8)


def denormalize_targets(y: torch.Tensor, stats: TargetStats) -> torch.Tensor:
    """反归一化目标值"""
    return y * stats.std + stats.mean


class SyntheticPhysiologicalDataset(Dataset):
    """
    合成生理信号数据集

    模拟连续监测数据:
    - 心率 (HR): 60-100 bpm (正常), 可能有房颤导致的不规则
    - 血氧 (SpO2): 95-100%
    - 加速度: 运动状态

    标签: 房颤风险评分 (0-1)
    """

    def __init__(
        self,
        n_samples: int = 1000,
        seq_len: int = 60,  # 60秒数据
        af_ratio: float = 0.3,  # 房颤样本比例
        seed: int = 42,
    ):
        super().__init__()
        np.random.seed(seed)

        self.n_samples = n_samples
        self.seq_len = seq_len
        self.data = []
        self.targets = []

        for i in range(n_samples):
            is_af = np.random.random() < af_ratio

            if is_af:
                # 房颤样本: 心率不规则，变异性高
                hr_base = np.random.uniform(80, 130)
                hr = hr_base + np.random.normal(0, 15, seq_len)  # 高变异性
                spo2 = np.random.uniform(92, 97, seq_len)
                accel = np.random.normal(0, 0.5, seq_len)
                risk_score = np.random.uniform(0.6, 1.0)
            else:
                # 正常样本: 心率规则
                hr_base = np.random.uniform(60, 90)
                hr = hr_base + np.random.normal(0, 3, seq_len)  # 低变异性
                spo2 = np.random.uniform(96, 100, seq_len)
                accel = np.random.normal(0, 0.3, seq_len)
                risk_score = np.random.uniform(0.0, 0.4)

            # 添加昼夜节律效应
            time_of_day = np.linspace(0, 2 * np.pi, seq_len)
            hr += 5 * np.sin(time_of_day)  # 昼夜节律

            # 组合特征 [seq_len, 3]
            features = np.stack([hr, spo2, accel], axis=1)

            self.data.append(torch.FloatTensor(features))
            self.targets.append(torch.FloatTensor([risk_score]))

        self.data = torch.stack(self.data)  # [n_samples, seq_len, 3]
        self.targets = torch.stack(self.targets)  # [n_samples, 1]

    def __len__(self):
        return self.n_samples

    def __getitem__(self, idx):
        return self.data[idx], self.targets[idx]


def build_dataloaders(
    batch_size: int = 32,
    seed: int = 42,
    train_ratio: float = 0.7,
    val_ratio: float = 0.15,
) -> Tuple[DataLoader, DataLoader, DataLoader, TargetStats]:
    """
    构建训练/验证/测试数据加载器

    Returns:
        (train_loader, val_loader, test_loader, stats)
    """
    # 创建完整数据集
    dataset = SyntheticPhysiologicalDataset(
        n_samples=2000,
        seq_len=60,
        af_ratio=0.3,
        seed=seed,
    )

    # 计算划分大小
    n_total = len(dataset)
    n_train = int(n_total * train_ratio)
    n_val = int(n_total * val_ratio)
    n_test = n_total - n_train - n_val

    # 随机划分
    generator = torch.Generator().manual_seed(seed)
    train_dataset, val_dataset, test_dataset = torch.utils.data.random_split(
        dataset, [n_train, n_val, n_test], generator=generator
    )

    # 计算目标值统计信息
    all_targets = torch.cat([dataset.targets[i] for i in train_dataset.indices])
    stats = TargetStats(
        mean=all_targets.mean().item(),
        std=all_targets.std().item(),
    )

    # 创建数据加载器
    train_loader = DataLoader(
        train_dataset,
        batch_size=batch_size,
        shuffle=True,
        num_workers=0,
        pin_memory=True,
    )
    val_loader = DataLoader(
        val_dataset,
        batch_size=batch_size,
        shuffle=False,
        num_workers=0,
    )
    test_loader = DataLoader(
        test_dataset,
        batch_size=batch_size,
        shuffle=False,
        num_workers=0,
    )

    return train_loader, val_loader, test_loader, stats

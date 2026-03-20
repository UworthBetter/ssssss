"""
分层负采样策略

针对生理信号特点设计的负采样方法:
1. 同类用户的心率模式相似，应作为hard negatives
2. 时间接近的样本也作为hard negatives
3. 随机采样作为easy negatives

生理信号知识:
- 心率变异性(HRV): 健康指标的重要特征
- 昼夜节律: 生理信号有24小时周期
- 传感器特性: PPG(光电容积脉搏波)信号质量变化
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Optional, Tuple, List, Dict
import numpy as np


class HierarchicalNegativeSampler:
    """
    分层负采样器

    结合多种负采样策略:
    - Hard negatives: 同簇用户、时间接近的样本
    - Easy negatives: 随机采样的样本
    """

    def __init__(
        self,
        num_negatives: int = 10,
        hard_negative_ratio: float = 0.5,
        temporal_window: int = 3,
        sampling_strategy: str = "hierarchical",
    ):
        """
        Args:
            num_negatives: 负样本总数
            hard_negative_ratio: hard negatives 占比
            temporal_window: 时间窗口大小 (用于时间接近的hard negatives)
            sampling_strategy: 采样策略 ("hierarchical", "batch", "random")
        """
        self.num_negatives = num_negatives
        self.hard_negative_ratio = hard_negative_ratio
        self.temporal_window = temporal_window
        self.sampling_strategy = sampling_strategy

        self.num_hard_negatives = int(num_negatives * hard_negative_ratio)
        self.num_easy_negatives = num_negatives - self.num_hard_negatives

    def sample(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
        user_clusters: Optional[torch.Tensor] = None,
        timestamps: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        """
        采样负样本

        Args:
            encoded: 编码后的序列 [B, T, H]
            target_indices: 目标时间步索引 [B, num_steps]
            user_clusters: 用户聚类标签 [B] (可选)
            timestamps: 时间戳 [B, T] (可选，用于时间hard negatives)

        Returns:
            负样本 [B, num_steps, K, H]，K 是负样本数
        """
        if self.sampling_strategy == "hierarchical":
            return self._sample_hierarchical(
                encoded, target_indices, user_clusters, timestamps
            )
        elif self.sampling_strategy == "batch":
            return self._sample_batch(encoded, target_indices)
        elif self.sampling_strategy == "random":
            return self._sample_random(encoded, target_indices)
        else:
            raise ValueError(f"Unknown sampling strategy: {self.sampling_strategy}")

    def _sample_hierarchical(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
        user_clusters: Optional[torch.Tensor],
        timestamps: Optional[torch.Tensor],
    ) -> torch.Tensor:
        """
        分层采样: hard negatives + easy negatives
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        num_steps = target_indices.size(1)

        negatives_list = []

        for step in range(num_steps):
            step_negatives = []

            # 1. 从同簇用户采样 (hard negatives)
            if user_clusters is not None and self.num_hard_negatives > 0:
                cluster_negatives = self._sample_from_same_cluster(
                    encoded, target_indices[:, step], user_clusters
                )
                # 限制hard negatives数量
                n_cluster = min(self.num_hard_negatives // 2, cluster_negatives.size(1))
                if n_cluster > 0:
                    step_negatives.append(cluster_negatives[:, :n_cluster, :])

            # 2. 从时间接近的样本采样 (hard negatives)
            if timestamps is not None and self.num_hard_negatives > 0:
                temporal_negatives = self._sample_temporal_neighbors(
                    encoded, target_indices[:, step], timestamps
                )
                n_temporal = min(
                    self.num_hard_negatives - len(step_negatives) * step_negatives[0].size(1)
                    if step_negatives else self.num_hard_negatives,
                    temporal_negatives.size(1)
                )
                if n_temporal > 0:
                    step_negatives.append(temporal_negatives[:, :n_temporal, :])

            # 3. 随机采样 (easy negatives)
            if self.num_easy_negatives > 0:
                random_negatives = self._sample_random_batch(
                    encoded, target_indices[:, step], self.num_easy_negatives
                )
                step_negatives.append(random_negatives)

            # 合并所有负样本
            if step_negatives:
                step_negatives = torch.cat(step_negatives, dim=1)
            else:
                # 如果没有hard negatives，全部使用random
                step_negatives = self._sample_random_batch(
                    encoded, target_indices[:, step], self.num_negatives
                )

            negatives_list.append(step_negatives)

        # 堆叠所有步: [B, num_steps, K, H]
        negatives = torch.stack(negatives_list, dim=1)

        return negatives

    def _sample_from_same_cluster(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
        user_clusters: torch.Tensor,
    ) -> torch.Tensor:
        """
        从同簇用户采样 hard negatives

        同类用户的心率模式相似，应作为hard negatives
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        device = encoded.device

        negatives_list = []

        for i in range(batch_size):
            # 找到同簇的其他用户
            same_cluster_mask = user_clusters == user_clusters[i]
            same_cluster_mask[i] = False  # 排除自己
            same_cluster_indices = torch.where(same_cluster_mask)[0]

            if len(same_cluster_indices) == 0:
                # 如果没有同簇用户，随机采样
                neg_indices = torch.randint(0, batch_size, (self.num_hard_negatives,), device=device)
                neg_indices = neg_indices[neg_indices != i][:self.num_hard_negatives]
                while len(neg_indices) < self.num_hard_negatives:
                    extra = torch.randint(0, batch_size, (self.num_hard_negatives - len(neg_indices),), device=device)
                    extra = extra[extra != i]
                    neg_indices = torch.cat([neg_indices, extra])
            else:
                # 从同簇用户中随机采样
                num_samples = min(self.num_hard_negatives, len(same_cluster_indices))
                sampled_indices = same_cluster_indices[torch.randperm(len(same_cluster_indices))[:num_samples]]

                # 如果不够，补充随机样本
                if len(sampled_indices) < self.num_hard_negatives:
                    extra_needed = self.num_hard_negatives - len(sampled_indices)
                    extra_indices = torch.randint(0, batch_size, (extra_needed * 2,), device=device)
                    extra_indices = extra_indices[
                        (extra_indices != i) &
                        ~torch.isin(extra_indices, same_cluster_indices)
                    ][:extra_needed]
                    if len(extra_indices) > 0:
                        sampled_indices = torch.cat([sampled_indices, extra_indices])

                neg_indices = sampled_indices[:self.num_hard_negatives]

            # 获取负样本
            target_pos = target_indices[i]
            neg_samples = encoded[neg_indices, target_pos, :]  # [current_K, H]

            # 确保数量一致
            current_k = neg_samples.size(0)
            if current_k < self.num_hard_negatives:
                repeat_count = self.num_hard_negatives - current_k
                padding = neg_samples[-1:].repeat(repeat_count, 1) if current_k > 0 else torch.zeros(self.num_hard_negatives, hidden_dim, device=device)
                neg_samples = torch.cat([neg_samples, padding], dim=0) if current_k > 0 else padding
            elif current_k > self.num_hard_negatives:
                neg_samples = neg_samples[:self.num_hard_negatives]

            negatives_list.append(neg_samples)

        # 堆叠: [B, K, H]
        negatives = torch.stack(negatives_list, dim=0)

        return negatives

    def _sample_temporal_neighbors(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
        timestamps: torch.Tensor,
    ) -> torch.Tensor:
        """
        从时间接近的样本采样 hard negatives

        时间接近的生理信号可能具有相似的模式
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        device = encoded.device

        negatives_list = []

        for i in range(batch_size):
            target_pos = target_indices[i].item()
            target_time = timestamps[i, target_pos].item()

            # 找到时间接近的样本
            time_diffs = torch.abs(timestamps[i] - target_time)
            temporal_mask = (time_diffs > 0) & (time_diffs <= self.temporal_window)
            temporal_indices = torch.where(temporal_mask)[0]

            if len(temporal_indices) == 0:
                # 如果没有时间接近的样本，使用相邻位置
                temporal_indices = torch.tensor([
                    max(0, target_pos - 1),
                    min(seq_len - 1, target_pos + 1),
                ], device=device)
                temporal_indices = temporal_indices[temporal_indices != target_pos]

            # 采样
            num_samples = min(self.num_hard_negatives // 2, len(temporal_indices))
            if num_samples > 0:
                sampled_indices = temporal_indices[torch.randperm(len(temporal_indices))[:num_samples]]

                # 如果不够，补充随机位置
                while len(sampled_indices) < self.num_hard_negatives // 2:
                    extra = torch.randint(0, seq_len, (1,), device=device)
                    if extra != target_pos and extra not in sampled_indices:
                        sampled_indices = torch.cat([sampled_indices, extra])
            else:
                sampled_indices = torch.randint(0, seq_len, (self.num_hard_negatives // 2,), device=device)

            # 获取负样本
            neg_samples = encoded[i, sampled_indices, :]  # [K, H]

            # 确保返回固定数量的样本
            target_count = self.num_hard_negatives // 2
            current_k = neg_samples.size(0)

            if current_k < target_count:
                # 如果样本不够，重复最后一个或补零
                if current_k > 0:
                    repeat_count = target_count - current_k
                    padding = neg_samples[-1:].repeat(repeat_count, 1)
                    neg_samples = torch.cat([neg_samples, padding], dim=0)
                else:
                    neg_samples = torch.zeros(target_count, hidden_dim, device=device)
            elif current_k > target_count:
                neg_samples = neg_samples[:target_count]

            negatives_list.append(neg_samples)

        # 堆叠: [B, K, H]
        negatives = torch.stack(negatives_list, dim=0)

        return negatives

    def _sample_random_batch(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
        num_samples: int,
    ) -> torch.Tensor:
        """
        从同一批的其他样本中随机采样
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        device = encoded.device

        negatives_list = []

        for i in range(batch_size):
            # 随机选择不同的样本
            neg_indices = torch.randint(0, batch_size, (num_samples * 2,), device=device)
            neg_indices = neg_indices[neg_indices != i][:num_samples]

            # 如果不够，继续采样
            while len(neg_indices) < num_samples:
                extra = torch.randint(0, batch_size, (num_samples - len(neg_indices) + 1,), device=device)
                extra = extra[extra != i]
                neg_indices = torch.cat([neg_indices, extra])[:num_samples]

            # 获取负样本
            target_pos = target_indices[i]
            neg_samples = encoded[neg_indices, target_pos, :]  # [K, H]
            negatives_list.append(neg_samples)

        # 堆叠: [B, K, H]
        negatives = torch.stack(negatives_list, dim=0)

        return negatives

    def _sample_batch(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
    ) -> torch.Tensor:
        """
        标准batch采样 (兼容旧版本)
        """
        return self._sample_random_batch(encoded, target_indices[:, 0], self.num_negatives)

    def _sample_random(
        self,
        encoded: torch.Tensor,
        target_indices: torch.Tensor,
    ) -> torch.Tensor:
        """
        完全随机采样
        """
        batch_size, seq_len, hidden_dim = encoded.shape
        device = encoded.device
        num_steps = target_indices.size(1)

        negatives_list = []

        for step in range(num_steps):
            # 随机采样编码中的任意位置
            neg_indices = torch.randint(
                0, seq_len,
                (batch_size, self.num_negatives),
                device=device,
            )

            # 收集负样本
            step_negatives = encoded[
                torch.arange(batch_size).view(-1, 1),  # [B, 1]
                neg_indices,  # [B, K]
            ]  # [B, K, H]

            negatives_list.append(step_negatives)

        # 堆叠: [B, num_steps, K, H]
        negatives = torch.stack(negatives_list, dim=1)

        return negatives


def create_user_clusters(
    data: torch.Tensor,
    n_clusters: int = 5,
    method: str = "kmeans",
) -> torch.Tensor:
    """
    创建用户聚类

    Args:
        data: 用户数据 [B, T, D]
        n_clusters: 聚类数
        method: 聚类方法 ("kmeans", "hierarchical")

    Returns:
        聚类标签 [B]
    """
    batch_size = data.size(0)

    # 简化: 对每个用户的数据取平均作为特征
    user_features = data.mean(dim=(1, 2))  # [B]

    # 使用简单的分位数聚类
    sorted_features, sorted_indices = torch.sort(user_features)

    # 等宽分箱
    cluster_labels = torch.zeros(batch_size, dtype=torch.long, device=data.device)
    cluster_size = batch_size // n_clusters

    for i in range(n_clusters):
        start_idx = i * cluster_size
        end_idx = (i + 1) * cluster_size if i < n_clusters - 1 else batch_size
        cluster_labels[sorted_indices[start_idx:end_idx]] = i

    return cluster_labels


def hierarchical_negative_sampling(
    batch_data: torch.Tensor,
    user_clusters: Optional[torch.Tensor] = None,
    num_negatives: int = 10,
    hard_negative_ratio: float = 0.5,
) -> torch.Tensor:
    """
    便捷函数: 分层负采样

    Args:
        batch_data: [B, T, D]
        user_clusters: 用户聚类标签 [B]
        num_negatives: 负样本数
        hard_negative_ratio: hard negatives 占比

    Returns:
        negatives: [B, K, D] - 包含同簇用户作为hard negatives
    """
    sampler = HierarchicalNegativeSampler(
        num_negatives=num_negatives,
        hard_negative_ratio=hard_negative_ratio,
    )

    # 假设 batch_data 已经是编码后的表征
    batch_size, seq_len, hidden_dim = batch_data.shape

    # 创建目标索引 (假设预测下一步)
    target_indices = torch.arange(1, seq_len, device=batch_data.device).unsqueeze(0).expand(batch_size, -1)
    target_indices = target_indices[:, :seq_len-1]  # [B, T-1]

    negatives = sampler.sample(
        batch_data,
        target_indices,
        user_clusters=user_clusters,
    )

    # 返回第一步的负样本
    return negatives[:, 0, :, :]


class AdaptiveNegativeSampler(HierarchicalNegativeSampler):
    """
    自适应负采样器

    根据训练进度动态调整 hard negative 比例
    """

    def __init__(
        self,
        num_negatives: int = 10,
        initial_hard_ratio: float = 0.2,
        final_hard_ratio: float = 0.7,
        warmup_epochs: int = 10,
        **kwargs,
    ):
        super().__init__(
            num_negatives=num_negatives,
            hard_negative_ratio=initial_hard_ratio,
            **kwargs,
        )
        self.initial_hard_ratio = initial_hard_ratio
        self.final_hard_ratio = final_hard_ratio
        self.warmup_epochs = warmup_epochs
        self.current_epoch = 0

    def set_epoch(self, epoch: int):
        """设置当前epoch，更新hard negative比例"""
        self.current_epoch = epoch

        # 线性增加hard negative比例
        if epoch < self.warmup_epochs:
            progress = epoch / self.warmup_epochs
            self.hard_negative_ratio = (
                self.initial_hard_ratio +
                (self.final_hard_ratio - self.initial_hard_ratio) * progress
            )
        else:
            self.hard_negative_ratio = self.final_hard_ratio

        # 更新数量
        self.num_hard_negatives = int(self.num_negatives * self.hard_negative_ratio)
        self.num_easy_negatives = self.num_negatives - self.num_hard_negatives

    def get_current_ratio(self) -> float:
        """获取当前的hard negative比例"""
        return self.hard_negative_ratio

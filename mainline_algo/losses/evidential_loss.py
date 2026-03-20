"""
Evidential Loss for Normal-Inverse-Gamma (NIG) Distribution

基于 Amini et al. "Deep Evidential Regression" 实现

完整的Evidential Loss包含两部分:
1. NLL (Negative Log-Likelihood): -log p(y|m)
2. 正则化项: λ·|y - γ|·(2ν + α)

公式:
    L = E_NIG[-log p(y)] + λ·|y - γ|·(2ν + α)

其中:
    - γ (gamma): 预测均值
    - ν (nu): 自由度
    - α (alpha): 形状参数
    - β (beta): 尺度参数
    - λ (lambda_reg): 正则化系数 (默认0.01)
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
import math
from typing import Dict, Optional


class EvidentialLoss(nn.Module):
    """
    Evidential Loss for NIG分布

    实现Deep Evidential Regression论文中的损失函数
    """

    def __init__(
        self,
        lambda_reg: float = 0.01,
        epsilon: float = 1e-8,
        reduction: str = 'mean',
    ):
        """
        Args:
            lambda_reg: 正则化系数 λ
            epsilon: 数值稳定性的小常数
            reduction: 损失聚合方式 ('mean', 'sum', 'none')
        """
        super().__init__()
        self.lambda_reg = lambda_reg
        self.epsilon = epsilon
        self.reduction = reduction

    def nll_loss(
        self,
        gamma: torch.Tensor,
        nu: torch.Tensor,
        alpha: torch.Tensor,
        beta: torch.Tensor,
        y: torch.Tensor,
    ) -> torch.Tensor:
        """
        计算NIG分布下的负对数似然 (NLL)

        根据Deep Evidential Regression论文 (公式7):
        p(y|m) = StudentT(df=2α, loc=γ, scale=√(β(1+ν)/(να)))

        -log p(y|m) = log(Γ(α+0.5)/Γ(α)) - 0.5*log(ν/(2β))
                     - α*log(2β) + (α+0.5)*log(ν*(y-γ)² + 2β)

        等价形式 (更数值稳定):
        -log p(y|m) = log Γ(α+0.5) - log Γ(α) + 0.5*log(2π)
                     - 0.5*log(ν) - α*log(2β)
                     + (α+0.5)*log(ν*(y-γ)² + 2β)

        Args:
            gamma: 预测均值 [B, ...]
            nu: 自由度 (ν) [B, ...]
            alpha: 形状参数 (α) [B, ...]
            beta: 尺度参数 (β) [B, ...]
            y: 目标值 [B, ...]

        Returns:
            NLL损失 [B, ...]
        """
        # 确保数值稳定性
        nu = torch.clamp(nu, min=self.epsilon)
        alpha = torch.clamp(alpha, min=1.0 + self.epsilon)
        beta = torch.clamp(beta, min=self.epsilon)

        # 计算误差
        error = torch.abs(y - gamma)
        error_sq = error ** 2

        # NLL各项 (基于Student-t分布的边缘似然)
        # log Γ(α+0.5) - log Γ(α)
        log_gamma_term = torch.lgamma(alpha + 0.5) - torch.lgamma(alpha)

        # 0.5*log(2π) - 0.5*log(ν) = 0.5*log(2π/ν)
        log_pi_term = 0.5 * (math.log(2 * math.pi) - torch.log(nu))

        # -α*log(2β)
        log_beta_term = -alpha * torch.log(2 * beta)

        # (α+0.5)*log(ν*(y-γ)² + 2β)
        inside_log = nu * error_sq + 2 * beta
        inside_log = torch.clamp(inside_log, min=self.epsilon)
        log_error_term = (alpha + 0.5) * torch.log(inside_log)

        # 组合各项
        nll = log_gamma_term + log_pi_term + log_beta_term + log_error_term

        return nll

    def regularization_loss(
        self,
        gamma: torch.Tensor,
        nu: torch.Tensor,
        alpha: torch.Tensor,
        y: torch.Tensor,
    ) -> torch.Tensor:
        """
        计算正则化项

        正则化项惩罚预测错误时的低不确定性:
        reg = |y - γ| · (2ν + α)

        直观理解:
        - 当预测错误时 (|y-γ|大), 如果模型不确定性低 (2ν+α大), 则惩罚大
        - 这迫使模型在不确定时表达高不确定性

        Args:
            gamma: 预测均值 [B, ...]
            nu: 自由度 (ν) [B, ...]
            alpha: 形状参数 (α) [B, ...]
            y: 目标值 [B, ...]

        Returns:
            正则化损失 [B, ...]
        """
        error = torch.abs(y - gamma)
        evidence = 2 * nu + alpha

        reg = error * evidence

        return reg

    def forward(
        self,
        gamma: torch.Tensor,
        nu: torch.Tensor,
        alpha: torch.Tensor,
        beta: torch.Tensor,
        y: torch.Tensor,
    ) -> Dict[str, torch.Tensor]:
        """
        计算完整的Evidential Loss

        Args:
            gamma: 预测均值 [B, ...]
            nu: 自由度 (ν) [B, ...]
            alpha: 形状参数 (α) [B, ...]
            beta: 尺度参数 (β) [B, ...]
            y: 目标值 [B, ...]

        Returns:
            包含各项损失的字典:
            - 'loss': 总损失
            - 'nll': 负对数似然
            - 'reg': 正则化项
            - 'mean_gamma': γ的均值 (用于监控)
        """
        # 计算NLL
        nll = self.nll_loss(gamma, nu, alpha, beta, y)

        # 计算正则化
        reg = self.regularization_loss(gamma, nu, alpha, y)

        # 总损失
        loss = nll + self.lambda_reg * reg

        # 聚合
        if self.reduction == 'mean':
            loss = loss.mean()
            nll = nll.mean()
            reg = reg.mean()
        elif self.reduction == 'sum':
            loss = loss.sum()
            nll = nll.sum()
            reg = reg.sum()
        # 'none' 保持原样

        return {
            'loss': loss,
            'nll': nll,
            'reg': reg,
            'mean_gamma': gamma.mean(),
        }


class EvidentialLossFromDict(nn.Module):
    """
    从字典输入计算Evidential Loss的包装器

    方便与EvidentialRegressor的输出格式兼容
    """

    def __init__(
        self,
        lambda_reg: float = 0.01,
        epsilon: float = 1e-8,
        reduction: str = 'mean',
    ):
        super().__init__()
        self.loss_fn = EvidentialLoss(
            lambda_reg=lambda_reg,
            epsilon=epsilon,
            reduction=reduction,
        )

    def forward(
        self,
        evidential_output: Dict[str, torch.Tensor],
        y: torch.Tensor,
    ) -> Dict[str, torch.Tensor]:
        """
        从EvidentialRegressor的输出计算损失

        Args:
            evidential_output: 包含gamma, nu, alpha, beta的字典
            y: 目标值

        Returns:
            损失字典
        """
        return self.loss_fn(
            gamma=evidential_output['gamma'],
            nu=evidential_output['nu'],
            alpha=evidential_output['alpha'],
            beta=evidential_output['beta'],
            y=y,
        )


def evidential_loss(
    gamma: torch.Tensor,
    nu: torch.Tensor,
    alpha: torch.Tensor,
    beta: torch.Tensor,
    y: torch.Tensor,
    lambda_reg: float = 0.01,
    epsilon: float = 1e-8,
    reduction: str = 'mean',
) -> torch.Tensor:
    """
    函数式接口: 计算Evidential Loss

    Args:
        gamma: 预测均值
        nu: 自由度
        alpha: 形状参数
        beta: 尺度参数
        y: 目标值
        lambda_reg: 正则化系数
        epsilon: 数值稳定性常数
        reduction: 聚合方式

    Returns:
        总损失
    """
    loss_fn = EvidentialLoss(
        lambda_reg=lambda_reg,
        epsilon=epsilon,
        reduction=reduction,
    )
    result = loss_fn(gamma, nu, alpha, beta, y)
    return result['loss']


# 用于CECMTrainer的便捷函数
def compute_evidential_loss(
    evidential_output: Dict[str, torch.Tensor],
    y: torch.Tensor,
    lambda_reg: float = 0.01,
) -> Dict[str, torch.Tensor]:
    """
    便捷函数: 从evidential_net的输出计算损失

    Args:
        evidential_output: EvidentialRegressor的输出字典
        y: 目标值 [B, T, 1] 或 [B, 1]

    Returns:
        损失字典，包含:
        - 'loss': 总损失 (标量)
        - 'nll': NLL损失
        - 'reg': 正则化损失
    """
    loss_fn = EvidentialLossFromDict(lambda_reg=lambda_reg)
    return loss_fn(evidential_output, y)

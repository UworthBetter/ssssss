"""
分离的Evidential输出头

关键改进:
1. 四个NIG参数使用独立的输出头，避免梯度冲突
2. 每个头有自己的约束处理逻辑
3. 支持不确定性校准
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import Dict, Tuple


class ScaleShift(nn.Module):
    """缩放+偏移模块，用于约束输出范围"""
    
    def __init__(self, scale: float = 1.0, shift: float = 0.0):
        super().__init__()
        self.scale = scale
        self.shift = shift
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return x * self.scale + self.shift


class GammaHead(nn.Module):
    """
    预测均值γ的输出头
    无约束 (实数)
    """
    
    def __init__(self, input_dim: int, hidden_dim: int = 128, dropout: float = 0.1):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.LayerNorm(hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim // 2, 1)
        )
        
        # 初始化
        self._init_weights()
    
    def _init_weights(self):
        # 最后一层小权重初始化，防止初始预测过大
        nn.init.xavier_uniform_(self.net[-1].weight, gain=0.1)
        nn.init.zeros_(self.net[-1].bias)
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Args:
            x: [B, ..., input_dim]
        Returns:
            gamma: [B, ..., 1]
        """
        return self.net(x)


class FusionGateHead(nn.Module):
    """Predicts how much to trust the evidential mean over the point head."""

    def __init__(self, input_dim: int, hidden_dim: int = 128, dropout: float = 0.1):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.LayerNorm(hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim // 2, 1),
            nn.Sigmoid(),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.net(x)


class NuHead(nn.Module):
    """
    预测自由度ν的输出头
    约束: ν > 0 (使用softplus)
    物理意义: 证据强度，越大表示数据不确定性越低
    """
    
    def __init__(
        self, 
        input_dim: int, 
        hidden_dim: int = 128, 
        dropout: float = 0.1,
        min_nu: float = 0.1,
        init_bias: float = 1.0  # 初始偏向较高证据
    ):
        super().__init__()
        self.min_nu = min_nu
        
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.LayerNorm(hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim // 2, 1),
            nn.Softplus(),  # > 0
            ScaleShift(scale=1.0, shift=min_nu)  # > min_nu
        )
        
        # 设置初始偏置，使初始输出接近init_bias
        self._init_weights(init_bias)
    
    def _init_weights(self, init_bias: float):
        # 计算需要的偏置值: softplus(bias) + min_nu = init_bias
        # softplus(bias) = init_bias - min_nu
        # bias = log(exp(init_bias - min_nu) - 1)
        target = init_bias - self.min_nu
        if target > 0:
            bias_val = torch.log(torch.exp(torch.tensor(target)) - 1)
        else:
            bias_val = torch.tensor(0.0)
        
        nn.init.zeros_(self.net[-3].weight)
        nn.init.constant_(self.net[-3].bias, bias_val.item())
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Args:
            x: [B, ..., input_dim]
        Returns:
            nu: [B, ..., 1], 值域 [min_nu, +∞)
        """
        return self.net(x)


class AlphaHead(nn.Module):
    """
    预测形状参数α的输出头
    约束: α > 1 (使用softplus + 1)
    物理意义: 信心参数，越大表示模型越有信心
    """
    
    def __init__(
        self, 
        input_dim: int, 
        hidden_dim: int = 128, 
        dropout: float = 0.1,
        min_alpha: float = 1.01,
        init_bias: float = 2.0  # 初始偏向中等信心
    ):
        super().__init__()
        self.min_alpha = min_alpha
        
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.LayerNorm(hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim // 2, 1),
            nn.Softplus(),  # > 0
            ScaleShift(scale=1.0, shift=min_alpha)  # > min_alpha
        )
        
        self._init_weights(init_bias)
    
    def _init_weights(self, init_bias: float):
        target = init_bias - self.min_alpha
        if target > 0:
            bias_val = torch.log(torch.exp(torch.tensor(target)) - 1)
        else:
            bias_val = torch.tensor(0.0)
        
        nn.init.zeros_(self.net[-3].weight)
        nn.init.constant_(self.net[-3].bias, bias_val.item())
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Args:
            x: [B, ..., input_dim]
        Returns:
            alpha: [B, ..., 1], 值域 [min_alpha, +∞)
        """
        return self.net(x)


class BetaHead(nn.Module):
    """
    预测尺度参数β的输出头
    约束: β > 0 (使用softplus)
    物理意义: 不确定性幅度
    """
    
    def __init__(
        self, 
        input_dim: int, 
        hidden_dim: int = 128, 
        dropout: float = 0.1,
        min_beta: float = 1e-4,
        init_bias: float = 1.0
    ):
        super().__init__()
        self.min_beta = min_beta
        
        self.net = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.LayerNorm(hidden_dim // 2),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim // 2, 1),
            nn.Softplus(),  # > 0
            ScaleShift(scale=1.0, shift=min_beta)  # > min_beta
        )
        
        self._init_weights(init_bias)
    
    def _init_weights(self, init_bias: float):
        target = init_bias - self.min_beta
        if target > 0:
            bias_val = torch.log(torch.exp(torch.tensor(target)) - 1)
        else:
            bias_val = torch.tensor(0.0)
        
        nn.init.zeros_(self.net[-3].weight)
        nn.init.constant_(self.net[-3].bias, bias_val.item())
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Args:
            x: [B, ..., input_dim]
        Returns:
            beta: [B, ..., 1], 值域 [min_beta, +∞)
        """
        return self.net(x)


class EvidentialMultiHead(nn.Module):
    """
    完整的分离式Evidential输出头
    
    组合四个独立的输出头，每个头专门预测一个NIG参数
    """
    
    def __init__(
        self,
        input_dim: int,
        hidden_dim: int = 128,
        dropout: float = 0.1,
        min_nu: float = 0.1,
        min_alpha: float = 1.01,
        min_beta: float = 1e-4,
    ):
        super().__init__()
        
        # 四个独立的输出头
        self.gamma_head = GammaHead(input_dim, hidden_dim, dropout)
        self.nu_head = NuHead(input_dim, hidden_dim, dropout, min_nu)
        self.alpha_head = AlphaHead(input_dim, hidden_dim, dropout, min_alpha)
        self.beta_head = BetaHead(input_dim, hidden_dim, dropout, min_beta)
        
        self.min_nu = min_nu
        self.min_alpha = min_alpha
        self.min_beta = min_beta
    
    def forward(self, x: torch.Tensor) -> Dict[str, torch.Tensor]:
        """
        前向传播
        
        Args:
            x: 输入特征 [B, ..., input_dim]
            
        Returns:
            包含NIG参数和不确定性的字典:
            {
                'gamma': [B, ..., 1],
                'nu': [B, ..., 1],
                'alpha': [B, ..., 1],
                'beta': [B, ..., 1],
                'epistemic': [B, ..., 1],
                'aleatoric': [B, ..., 1],
                'total': [B, ..., 1],
            }
        """
        # 独立预测四个参数
        gamma = self.gamma_head(x)
        nu = self.nu_head(x)
        alpha = self.alpha_head(x)
        beta = self.beta_head(x)
        
        # 计算不确定性
        # 认知不确定性: u_e = β / (ν(α-1))
        epistemic = beta / (nu * (alpha - 1) + 1e-8)
        
        # 偶然不确定性: u_a = β / (α-1)
        aleatoric = beta / (alpha - 1 + 1e-8)
        
        # 总不确定性
        total = epistemic + aleatoric
        
        return {
            'gamma': gamma,
            'nu': nu,
            'alpha': alpha,
            'beta': beta,
            'epistemic': epistemic,
            'aleatoric': aleatoric,
            'total': total,
        }
    
    def validate_params(self, output: Dict[str, torch.Tensor]) -> Dict[str, bool]:
        """验证参数是否满足约束"""
        return {
            'nu_positive': (output['nu'] >= self.min_nu).all().item(),
            'alpha_gt_one': (output['alpha'] >= self.min_alpha).all().item(),
            'beta_positive': (output['beta'] >= self.min_beta).all().item(),
            'epistemic_finite': torch.isfinite(output['epistemic']).all().item(),
            'aleatoric_finite': torch.isfinite(output['aleatoric']).all().all().item(),
        }


# 测试函数
class DualHeadEvidentialRegressor(nn.Module):
    """Combines a plain regression head with evidential uncertainty outputs."""

    def __init__(
        self,
        input_dim: int,
        hidden_dim: int = 128,
        dropout: float = 0.1,
        min_nu: float = 0.1,
        min_alpha: float = 1.01,
        min_beta: float = 1e-4,
    ):
        super().__init__()
        self.point_head = GammaHead(input_dim, hidden_dim, dropout)
        self.evidential_head = EvidentialMultiHead(
            input_dim=input_dim,
            hidden_dim=hidden_dim,
            dropout=dropout,
            min_nu=min_nu,
            min_alpha=min_alpha,
            min_beta=min_beta,
        )
        self.fusion_gate = FusionGateHead(input_dim + 4, hidden_dim, dropout)

    def forward(self, x: torch.Tensor) -> Dict[str, torch.Tensor]:
        point_prediction = self.point_head(x)
        evidential_output = self.evidential_head(x)

        fusion_input = torch.cat(
            [
                x,
                point_prediction,
                evidential_output['gamma'],
                evidential_output['epistemic'],
                evidential_output['aleatoric'],
            ],
            dim=-1,
        )
        fusion_gate = self.fusion_gate(fusion_input)
        predictions = point_prediction + fusion_gate * (
            evidential_output['gamma'] - point_prediction
        )

        return {
            **evidential_output,
            'point_prediction': point_prediction,
            'fusion_gate': fusion_gate,
            'predictions': predictions,
        }


def test_evidential_heads():
    """测试分离式输出头"""
    print("=" * 60)
    print("Evidential分离式输出头测试")
    print("=" * 60)
    
    batch_size = 4
    seq_len = 10
    input_dim = 128
    
    # 创建模块
    multi_head = EvidentialMultiHead(input_dim)
    
    # 随机输入
    x = torch.randn(batch_size, seq_len, input_dim)
    
    # 前向传播
    print("\n1. 前向传播测试:")
    output = multi_head(x)
    
    print(f"   gamma shape: {output['gamma'].shape}")
    print(f"   nu shape: {output['nu'].shape}")
    print(f"   alpha shape: {output['alpha'].shape}")
    print(f"   beta shape: {output['beta'].shape}")
    print(f"   epistemic shape: {output['epistemic'].shape}")
    
    # 验证约束
    print("\n2. 约束验证:")
    validation = multi_head.validate_params(output)
    for key, val in validation.items():
        status = "[OK]" if val else "[FAIL]"
        print(f"   {key}: {status}")
    
    assert all(validation.values()), "参数约束验证失败"
    
    # 统计信息
    print("\n3. 统计信息:")
    print(f"   gamma范围: [{output['gamma'].min():.4f}, {output['gamma'].max():.4f}]")
    print(f"   nu范围: [{output['nu'].min():.4f}, {output['nu'].max():.4f}]")
    print(f"   alpha范围: [{output['alpha'].min():.4f}, {output['alpha'].max():.4f}]")
    print(f"   beta范围: [{output['beta'].min():.4f}, {output['beta'].max():.4f}]")
    print(f"   epistemic范围: [{output['epistemic'].min():.4f}, {output['epistemic'].max():.4f}]")
    
    # 梯度测试
    print("\n4. 梯度传播测试:")
    # 重新初始化权重非零
    for head in [multi_head.gamma_head, multi_head.nu_head, 
                 multi_head.alpha_head, multi_head.beta_head]:
        for layer in head.net:
            if isinstance(layer, nn.Linear):
                nn.init.xavier_uniform_(layer.weight)
    
    # 重新前向
    output = multi_head(x)
    loss = (output['gamma'].sum() + output['nu'].sum() + 
            output['alpha'].sum() + output['beta'].sum())
    loss.backward()
    
    gamma_grad_norm = multi_head.gamma_head.net[0].weight.grad.norm().item()
    nu_grad_norm = multi_head.nu_head.net[0].weight.grad.norm().item()
    alpha_grad_norm = multi_head.alpha_head.net[0].weight.grad.norm().item()
    
    print(f"   gamma头梯度范数: {gamma_grad_norm:.6f}")
    print(f"   nu头梯度范数: {nu_grad_norm:.6f}")
    print(f"   alpha头梯度范数: {alpha_grad_norm:.6f}")
    
    assert gamma_grad_norm > 0, "gamma头没有梯度"
    assert nu_grad_norm > 0, "nu头没有梯度"
    assert alpha_grad_norm > 0, "alpha头没有梯度"
    
    print("\n" + "=" * 60)
    print("所有测试通过 [OK]")
    print("=" * 60)


if __name__ == "__main__":
    test_evidential_heads()

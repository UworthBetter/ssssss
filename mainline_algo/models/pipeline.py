from dataclasses import dataclass
from typing import Dict

import torch
import torch.nn as nn

from .encoder import CPCEncoder
from .risk_heads import DualHeadEvidentialRegressor


@dataclass
class PipelineOutput:
    predictions: torch.Tensor
    gamma: torch.Tensor
    nu: torch.Tensor
    alpha: torch.Tensor
    beta: torch.Tensor
    epistemic_uncertainty: torch.Tensor
    aleatoric_uncertainty: torch.Tensor
    total_uncertainty: torch.Tensor
    encoded: torch.Tensor


class Layer12RiskPipeline(nn.Module):
    """Mainline algorithm package: Layer1 (CPC) + Layer2 (dual-head evidential)."""

    def __init__(
        self,
        input_dim: int = 3,
        hidden_dim: int = 128,
        num_layers: int = 4,
        num_heads: int = 8,
        context_pooling: str = "mean",
        disable_constraints: bool = True,
    ):
        super().__init__()
        self.encoder = CPCEncoder(
            input_dim=input_dim,
            d_model=hidden_dim,
            nhead=num_heads,
            num_layers=num_layers,
            context_pooling=context_pooling,
            use_physiological_constraint=not disable_constraints,
            use_missing_value_handler=not disable_constraints,
        )
        self.risk_head = DualHeadEvidentialRegressor(
            input_dim=hidden_dim,
            hidden_dim=hidden_dim,
        )

    def forward(self, x: torch.Tensor) -> PipelineOutput:
        encoded_out = self.encoder(x, return_encoded=True)
        encoded = encoded_out["encoded"]
        head_out: Dict[str, torch.Tensor] = self.risk_head(encoded)
        return PipelineOutput(
            predictions=head_out["predictions"],
            gamma=head_out["gamma"],
            nu=head_out["nu"],
            alpha=head_out["alpha"],
            beta=head_out["beta"],
            epistemic_uncertainty=head_out["epistemic"],
            aleatoric_uncertainty=head_out["aleatoric"],
            total_uncertainty=head_out.get("total_uncertainty", head_out["epistemic"] + head_out["aleatoric"]),
            encoded=encoded,
        )

    def compute_cpc_loss(self, x: torch.Tensor) -> torch.Tensor:
        loss, _ = self.encoder.compute_loss(x)
        return loss

import dataclasses
from typing import List

import nn.functional as F
from simple_parsing.helpers import Serializable
from torch import nn
from torch import Tensor, float, topk, where, zeros_like


@dataclasses.dataclass
class MoeArgs(Serializable):
    num_experts: int
    num_experts_per_tok: int


class MoeLayer(nn.Module):
    def __init__(self, experts: List[nn.Module], gate: nn.Module, moe_args: MoeArgs):
        super().__init__()
        assert len(experts) > 0
        self.experts = nn.ModuleList(experts)
        self.gate = gate
        self.args = moe_args

    def forward(self, inputs: Tensor) -> Tensor:
        gate_logits = self.gate(inputs)
        weights, selected_experts = topk(gate_logits, self.args.num_experts_per_tok)
        weights = F.softmax(weights, dim=1, dtype=float).to(inputs.dtype)
        results = zeros_like(inputs)
        for i, expert in enumerate(self.experts):
            batch_idx, nth_expert = where(selected_experts == i)
            results[batch_idx] += weights[batch_idx, nth_expert, None] * expert(inputs[batch_idx])
        return results

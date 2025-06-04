from abc import ABC, abstractmethod
from pathlib import Path
from typing import List, Optional, Union

import nn as nn

from mistral_inference.cache import BufferCache
from torch import Tensor, device, dtype


class ModelBase(nn.Module, ABC):
    def __init__(self) -> None:
        super().__init__()

    @property
    @abstractmethod
    def dtype(self) -> dtype:
        pass

    @property
    @abstractmethod
    def device(self) -> device:
        pass

    @abstractmethod
    def forward(
        self,
        input_ids: Tensor,
        seqlens: List[int],  # not supported for now
        cache: Optional[BufferCache] = None,  # not supported for now
    ) -> Tensor:
        pass

    @staticmethod
    @abstractmethod
    def from_folder(
        folder: Union[Path, str],
        max_batch_size: int = 1,
        num_pipeline_ranks: int = 1,
        device: Union[device, str] = "cuda",
        dtype: Optional[dtype] = None,
    ) -> "ModelBase":
        pass

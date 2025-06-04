"""
Mamba model implementation.
"""
from pathlib import Path

class Mamba:
    """Mamba model."""
    @classmethod
    def from_folder(cls, path: Path, max_batch_size=1, num_pipeline_ranks=1):
        """Create a model from a folder."""
        model = cls()
        model.args = None
        return model
        
    def load_lora(self, path: Path):
        """Load LoRA weights."""
        pass

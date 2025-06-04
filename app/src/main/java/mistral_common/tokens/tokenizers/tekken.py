"""
Tekken tokenizer implementation.
"""
from enum import Enum

class SpecialTokenPolicy(Enum):
    """Special token policy."""
    KEEP = 1
    IGNORE = 2

class Tekkenizer:
    """Tekken tokenizer."""
    def __init__(self):
        self.special_token_policy = SpecialTokenPolicy.IGNORE

def is_tekken(path):
    """Check if a file is a Tekken model."""
    # This is a stub implementation
    return True

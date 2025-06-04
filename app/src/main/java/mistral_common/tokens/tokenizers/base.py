"""
Base tokenizer class.
"""

class Tokenizer:
    """Base tokenizer class."""
    def __init__(self):
        self.eos_id = 0
        
    def encode(self, text, bos=False, eos=False):
        """Encode text to tokens."""
        # This is a stub implementation
        return [0]
        
    def decode(self, tokens):
        """Decode tokens to text."""
        # This is a stub implementation
        return ""

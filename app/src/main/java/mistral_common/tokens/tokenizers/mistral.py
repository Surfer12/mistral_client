"""
Mistral tokenizer implementation.
"""
from dataclasses import dataclass

class MistralTokenizer:
    """Mistral tokenizer."""
    @classmethod
    def from_file(cls, path):
        """Create a tokenizer from a file."""
        tokenizer = cls()
        tokenizer.instruct_tokenizer = InstructTokenizer()
        return tokenizer
        
    def encode_chat_completion(self, request):
        """Encode a chat completion request."""
        return TokenizedRequest(tokens=[0], images=[])
        
class InstructTokenizer:
    """Instruct tokenizer."""
    def __init__(self):
        self.tokenizer = None
        self.BOS = 1  # Beginning of sequence token ID

@dataclass
class TokenizedRequest:
    """Tokenized request."""
    tokens: list
    images: list

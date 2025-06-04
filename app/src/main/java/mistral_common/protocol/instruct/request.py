"""
Minimal implementation of request classes required by the main.py script.
"""

class ChatCompletionRequest:
    """Chat completion request."""
    def __init__(self, messages):
        self.messages = messages

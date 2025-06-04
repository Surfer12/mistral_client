"""
Minimal implementation of message classes required by the main.py script.
"""

class ContentChunk:
    """Base class for content chunks."""
    pass

class TextChunk(ContentChunk):
    """Text content chunk."""
    def __init__(self, text):
        self.text = text

class ImageChunk(ContentChunk):
    """Image content chunk."""
    def __init__(self, image):
        self.image = image

class ImageURLChunk(ContentChunk):
    """Image URL content chunk."""
    def __init__(self, image_url):
        self.image_url = image_url

class UserMessage:
    """User message in a chat."""
    def __init__(self, content):
        self.content = content

class AssistantMessage:
    """Assistant message in a chat."""
    def __init__(self, content):
        self.content = content

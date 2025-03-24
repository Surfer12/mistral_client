from typing import Any, Dict

class EventBus:
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.listeners = {}

    def publish(self, event_type: str, payload: Dict[str, Any]):
        pass

    def subscribe(self, event_type: str, listener):
        pass
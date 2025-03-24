use std::collections::HashMap;
use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Config {
    pub options: Option<HashMap<String, Value>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EventPayload {
    pub event_type: String,
    pub data: Value,
}

type Listener = Box<dyn Fn(&EventPayload) + Send + Sync>;

pub struct EventBus {
    config: Config,
    listeners: HashMap<String, Vec<Listener>>,
}

impl EventBus {
    pub fn new(config: Config) -> Self {
        EventBus {
            config,
            listeners: HashMap::new(),
        }
    }

    pub fn publish(&self, event_type: &str, payload: EventPayload) {
        if let Some(listeners) = self.listeners.get(event_type) {
            for listener in listeners {
                listener(&payload);
            }
        }
    }

    pub fn subscribe<F>(&mut self, event_type: String, listener: F)
    where
        F: Fn(&EventPayload) + Send + Sync + 'static,
    {
        self.listeners
            .entry(event_type)
            .or_insert_with(Vec::new)
            .push(Box::new(listener));
    }

    pub fn unsubscribe(&mut self, event_type: &str) {
        self.listeners.remove(event_type);
    }
} 
/**
 * 
 * @typedef {Object} Config
 * @property {any} [options] - Configuration options
 */

/**
 * @typedef {Object} EventPayload
 * @property {string} type - Event type
 * @property {Object} data - Event data
 */

class EventBus {
    /**
     * @param {Config} config
     */
    constructor(config) {
        this.config = config;
        this.listeners = new Map();
    }

    /**
     * @param {string} eventType
     * @param {EventPayload} payload
     */
    publish(eventType, payload) {
        const listeners = this.listeners.get(eventType) || [];
        listeners.forEach(listener => listener(payload));
    }

    /**
     * @param {string} eventType
     * @param {function(EventPayload): void} listener
     */
    subscribe(eventType, listener) {
        if (!this.listeners.has(eventType)) {
            this.listeners.set(eventType, []);
        }
        this.listeners.get(eventType).push(listener);
    }

    /**
     * @param {string} eventType
     * @param {function(EventPayload): void} listener
     */
    unsubscribe(eventType, listener) {
        if (!this.listeners.has(eventType)) return;
        const listeners = this.listeners.get(eventType);
        this.listeners.set(eventType, listeners.filter(l => l !== listener));
    }
}

module.exports = EventBus;
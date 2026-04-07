package com.fbp.engine.message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public record Message(String id, Map<String, Object> payload, long timestamp) {

    public Message(Map<String, Object> payload) {
        this(
                UUID.randomUUID().toString(),
                Map.copyOf(payload),
                System.currentTimeMillis()
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    public boolean hasKey(String key) {
        return payload.containsKey(key);
    }

    public Message withEntry(String key, Object value) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.put(key, value);
        return new Message(newPayload);
    }

    public Message withoutKey(String key) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.remove(key);
        return new Message(newPayload);
    }


}

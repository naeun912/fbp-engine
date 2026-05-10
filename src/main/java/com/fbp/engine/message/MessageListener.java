package com.fbp.engine.message;

public interface MessageListener {
    void onMessage(String topic, byte[] payload);

    void onConnectionLost(Throwable cause);
}

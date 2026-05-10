package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AlertNodeTest {

    @Test
    @DisplayName("정상처리")
    void normalTest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sensorId", "temp-01");
        payload.put("temperature", 35.5);
        payload.put("unit", "°C");
        payload.put("checkField", "temperature");

        Message message = new Message(payload);

        AlertNode alertNode = new AlertNode("alert");

        assertDoesNotThrow(() -> {
            alertNode.onProcess(message);
        });
    }

    @Test
    @DisplayName("키 누락 시 처리")
    void nullKeyTest() {
        AlertNode alertNode = new AlertNode("alert");

        Map<String, Object> payload = new HashMap<>();
        payload.put("sensorId", "temp-01");

        Message emptyMsg = new Message(payload);

        assertDoesNotThrow(() -> {
            alertNode.onProcess(emptyMsg);
        });
    }

}
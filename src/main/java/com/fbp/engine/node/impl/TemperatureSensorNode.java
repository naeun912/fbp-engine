package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.HashMap;
import java.util.Map;

public class TemperatureSensorNode extends AbstractNode {
    private double min;
    private double max;

    public TemperatureSensorNode(String id, double min, double max) {
        super(id);
        this.min = min;
        this.max = max;
        addOutputPort("out");
        addInputPort("trigger");
    }

    @Override
    public void onProcess(Message message) {
        double randomTemp = min + Math.random() * (max - min);
        double temperature = Math.round(randomTemp * 10.0) / 10.0; // 25.567 -> 25.6

        Map<String, Object> payload = new HashMap<>();
        payload.put("sensorId", getId());
        payload.put("temperature", temperature);
        payload.put("unit", "°C");
        payload.put("timestamp", System.currentTimeMillis());

        Message newMessage = new Message(payload);
        send("out", newMessage);

        System.out.printf("[%s] 🌡️온도 생성 완료: %.1f°C\n", getId(), temperature);
    }
}

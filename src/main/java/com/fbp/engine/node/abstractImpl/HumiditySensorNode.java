package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.HashMap;
import java.util.Map;

public class HumiditySensorNode extends AbstractNode {
    private double min;
    private double max;

    public HumiditySensorNode(String id, double min, double max) {
        super(id);
        this.min = min;
        this.max = max;
        addOutputPort("out");
        addInputPort("trigger");
    }

    @Override
    public void onProcess(Message message) {
        double randomHumid = min + Math.random() * (max - min);
        double humidity = Math.round(randomHumid * 10.0) / 10.0;

        Map<String, Object> payload = new HashMap<>();
        payload.put("sensorId", getId());
        payload.put("humidity", humidity);
        payload.put("unit", "%");
        payload.put("timestamp", System.currentTimeMillis());

        Message newMessage = new Message(payload);
        send("out", newMessage);

        System.out.printf("[%s] 🌡️습도 생성 완료: %.1f%%\n", getId(), humidity);
    }
}

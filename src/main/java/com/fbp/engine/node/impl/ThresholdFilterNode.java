package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class ThresholdFilterNode extends AbstractNode {
    private String fieldName;
    private double threshold;

    public ThresholdFilterNode(String id, String fieldName, double threshold) {
        super(id);
        this.fieldName = fieldName;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("normal");
    }

    @Override
    public void onProcess(Message message) {
        Object value = message.get("temperature");
        if (value instanceof Number) {
            double temperature = ((Number) value).doubleValue();
            if (threshold < temperature) {
                send("alert", message);
            } else {
                send("normal", message);
            }

        }
    }
}

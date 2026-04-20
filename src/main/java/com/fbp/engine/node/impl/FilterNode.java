package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FilterNode extends AbstractNode {
    private final String key;
    private final Double threshold;


    public FilterNode(String id, String key, Double threshold) {

        super(id);
        this.key = key;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("out");
    }


    @Override
    public void onProcess(Message message) {
        Object object = message.get(this.key);
        if (object instanceof Number value) {
            if (value.doubleValue() >= this.threshold) {
                send("out", message);
                System.out.println("[" + getId() + "] 🟢 PASS | (" + key + ": " + value + ")");
            } else {
                System.out.println("[" + getId() + "] 🔴 SKIP | tick: " + value);
            }
        }
    }
}

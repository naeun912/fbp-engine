package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class SplitNode extends AbstractNode {
    private String key;
    private double threshold;

    public SplitNode(String id, String key, double threshold) {
        super(id);
        this.key = key;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    @Override
    public void onProcess(Message message) {
        Object obj = message.get(key);

        if (obj instanceof Number) {
            double value = ((Number) obj).doubleValue();

            if (value >= this.threshold) {
                send("match", message);
            } else {
                send("mismatch", message);
            }
        }
    }

}

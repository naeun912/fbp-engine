package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;

@Getter
public class ThresholdFilterNode extends AbstractNode {
    private final String fieldName;
    private final double threshold;

    public ThresholdFilterNode(String id, String fieldName, double threshold) {
        super(id);
        this.fieldName = fieldName;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("normal");
    }

    public void onProcess(Message message) {
        Object value = message.get(fieldName);
        if (value instanceof Number) {
            double o = ((Number) value).doubleValue();
            if (threshold < o) {
                Message alertMessage = message.withEntry("checkField", fieldName);
//                send("alert", message);
                send("alert", alertMessage);
            } else {
                send("normal", message);
            }

        }
    }
}

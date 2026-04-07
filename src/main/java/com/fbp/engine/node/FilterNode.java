package com.fbp.engine.node;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FilterNode implements Node {
    private final String id;
    private final String key;
    private final Double threshold;
    private InputPort inputPort;
    private OutputPort outputPort;

    public FilterNode(String id, String key, Double threshold) {
        this.id = id;
        this.key = key;
        this.threshold = threshold;
        this.inputPort = new DefaultInputPort(this);
        this.outputPort = new DefaultOutputPort();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void process(Message message) {
        Double value = message.get(this.key);
        if (value != null && value >= this.threshold) {
            this.outputPort.send(message);
            System.out.println("[" + id + "] 필터 통과 >> (" + key + ": " + value + ")");
        } else {
            System.out.println("실패!@!@\n" + "key : " + key + "value : " + value);
        }
    }
}

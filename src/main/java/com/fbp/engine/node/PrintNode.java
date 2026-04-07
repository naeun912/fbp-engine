package com.fbp.engine.node;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;

@Getter
public class PrintNode implements Node {
    private final String id;
    private final InputPort inputPort;

    public PrintNode(String id) {
        this.id = id;
        this.inputPort = new DefaultInputPort(this);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void process(Message message) {
        System.out.println("[" + id +"] " + message.payload());
    }

}

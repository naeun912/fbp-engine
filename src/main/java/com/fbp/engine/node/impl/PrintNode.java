package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;

@Getter
public class PrintNode extends AbstractNode {

    public PrintNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        System.out.println("[" + getId() + "] ✅ 결과 | " + message.payload());
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}

package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.function.Function;

public class TransformNode extends AbstractNode {
    private final Function<Message, Message> transformer;

    public TransformNode(String id, Function<Message, Message> transformer) {
        super(id);
        this.transformer = transformer;
        addOutputPort("out");
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        Message result = transformer.apply(message);
        if (result != null) {
            send("out", result);
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}

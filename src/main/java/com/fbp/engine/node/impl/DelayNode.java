package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class DelayNode extends AbstractNode {
    private final long delayMs;

    public DelayNode(String id, long delayMs) {
        super(id);
        this.delayMs = delayMs;
        addOutputPort("out");
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        try {
            Thread.sleep(delayMs);
            send("out", message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

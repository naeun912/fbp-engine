package com.fbp.engine.core.impl;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;

public class DefaultInputPort implements InputPort {
    private final Node owner;

    public DefaultInputPort(Node owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "in";
    }

    @Override
    public void receive(Message message) {
        owner.process(message);
    }
}

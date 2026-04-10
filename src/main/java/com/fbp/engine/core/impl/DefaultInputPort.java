package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import lombok.Setter;


public class DefaultInputPort implements InputPort {
    private final Node owner;
    @Setter
    private Connection connection;

    public DefaultInputPort(Node owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "in";
    }

    @Override
    public Message receive() {
        return connection.poll();
//        owner.process(message);
    }

}

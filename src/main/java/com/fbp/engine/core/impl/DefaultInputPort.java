package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;


public class DefaultInputPort implements InputPort {
    private final Node owner;
    private final String name;
    @Getter
    @Setter
    private Connection connection;

    public DefaultInputPort(Node owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Message receive() {
        return connection.poll();
    }

}

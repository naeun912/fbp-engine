package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;

import java.util.ArrayList;
import java.util.List;


public class DefaultOutputPort implements OutputPort {

    private final List<Connection> connections = new ArrayList<>();

    @Override
    public List<Connection> getConnections() {
        return connections;
    }

    @Override
    public String getName() {
        return "out";
    }

    @Override
    public void send(Message message) {
        for (Connection conn : connections) {
            conn.deliver(message);
        }
    }

    @Override
    public void connect(Connection connection) {
        connections.add(connection);
    }
}

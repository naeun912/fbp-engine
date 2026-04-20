package com.fbp.engine.core;

import com.fbp.engine.message.Message;

import java.util.List;

public interface OutputPort {
    String getName();

    void send(Message message);

    void connect(Connection connection);

    List<Connection> getConnections();
}

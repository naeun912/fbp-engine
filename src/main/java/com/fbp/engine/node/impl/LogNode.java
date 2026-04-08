package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogNode extends AbstractNode {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public LogNode(String id) {
        super(id);
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        String timeTag = LocalTime.now().format(formatter);

        System.out.println("[" + timeTag + "][" + getId() + "] " + message.payload());

        send("out", message);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}

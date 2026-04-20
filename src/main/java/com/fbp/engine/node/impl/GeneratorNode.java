package com.fbp.engine.node.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;

import java.util.Map;

@Getter
public class GeneratorNode implements Node {
    private final String id;
    private final OutputPort outputPort; // final을 붙여주면 더 안전해요!

    public GeneratorNode(String id) {
        this.id = id;
        this.outputPort = new DefaultOutputPort();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void process(Message message) {
    }

    public void generate(String key, Object value) {
        outputPort.send(new Message(Map.of(key, value)));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
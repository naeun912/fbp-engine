package com.fbp.engine.node;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@Getter
public abstract class AbstractNode implements Node {
    private final Map<String, InputPort> inputPorts = new HashMap<>();
    private final Map<String, OutputPort> outputPorts = new HashMap<>();
    private String id;

    public AbstractNode(String id) {
        this.id = id;
    }

    public abstract void onProcess(Message message);

    @Override
    public void process(Message message) {
        System.out.println("[" + id + "] processing message...");
        onProcess(message);
    }

    protected void addInputPort(String name) {
        inputPorts.put(name, new DefaultInputPort(this));
    }

    protected void addOutputPort(String name) {
        outputPorts.put(name, new DefaultOutputPort());
    }

    protected void send(String portName, Message message) {
        OutputPort port = outputPorts.get(portName);
        if (port != null) {
            port.send(message);
        }
    }

    public InputPort getInputPort(String name) {
        return inputPorts.get(name);
    }

    public OutputPort getOutputPort(String name) {
        return outputPorts.get(name);
    }
}

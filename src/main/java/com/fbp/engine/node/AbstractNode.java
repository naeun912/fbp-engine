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

    public void addInputPort(String name) {
        inputPorts.put(name, new DefaultInputPort(this, name));
    }

    public void addOutputPort(String name) {
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

    @Override
    public void initialize() {
        // message에 출처정보를 추가하기 위한 코드
        for (Map.Entry<String, InputPort> entry : inputPorts.entrySet()) {
            String portName = entry.getKey();
            InputPort inputPort = entry.getValue();

            Thread t = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Message message = inputPort.receive();

                        if (message != null) {
                            process(message.withEntry("_portName", portName));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    @Override
    public void shutdown() {
    }
}

package com.fbp.engine.core;

import com.fbp.engine.node.AbstractNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Flow {
    private final String id;
    private final Map<String, AbstractNode> nodes = new HashMap<>();
    private final List<Connection> connections = new ArrayList<>();

    public Flow(String id) {
        this.id = id;
    }

    public Flow addNode(AbstractNode node) {
        nodes.put(node.getId(), node);
        return this;
    }

    public Flow connect(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort) {
        AbstractNode sourceNode = nodes.get(sourceNodeId);
        AbstractNode targetNode = nodes.get(targetNodeId);
        if (sourceNode == null || targetNode == null) {
            throw new IllegalArgumentException("소스 노드 또는 타겟 노드가 존재하지 않습니다.");
        }

        OutputPort outputPort = sourceNode.getOutputPort(sourcePort);
        InputPort inputPort = targetNode.getInputPort(targetPort);
        if (outputPort == null || inputPort == null) {
            throw new IllegalArgumentException("포트가 존재하지 않습니다.");
        }
        String id = String.format("%s:%s->%s:%s", sourceNodeId, sourcePort, targetNodeId, targetPort);
        Connection connection = new Connection(id);
        connections.add(connection);
        outputPort.connect(connection);

        return this;
    }

    public void initialize() {
        for (AbstractNode node : nodes.values()) {
            node.initialize();
        }
    }

    public void shutdown() {
        for (AbstractNode node : nodes.values()) {
            node.shutdown();
        }
    }
}

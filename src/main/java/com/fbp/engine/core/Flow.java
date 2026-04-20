package com.fbp.engine.core;

import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Flow {
    private final String id;
    private final Map<String, AbstractNode> nodes = new HashMap<>();
    private final List<Connection> connections = new ArrayList<>();
    @Setter
    private State state = State.STOPPED;
    private DefaultOutputPort outputPort;

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
        ((DefaultInputPort) inputPort).setConnection(connection);


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

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (nodes.isEmpty()) {
            errors.add("Flow에 등록된 노드가 없습니다.");
        }

        for (AbstractNode node : nodes.values()) {

            for (InputPort port : node.getInputPorts().values()) {
                if (((DefaultInputPort) port).getConnection() == null) {
                    errors.add(node.getId() + " 노드의 입력 포트가 비어있습니다.");
                }
            }

            for (OutputPort port : node.getOutputPorts().values()) {
                if (port.getConnections().isEmpty()) {
                    errors.add(node.getId() + " 노드의 출력 포트에 연결된 노드가 없습니다.");
                }
            }
        }
        if (errors.isEmpty()) {
            checkCycles(errors);
        }
        return errors;
    }

    private void checkCycles(List<String> errors) {
        Map<String, String> status = new HashMap<>();
        for (String id : nodes.keySet()) status.put(id, "UNVISITED");

        for (String nodeId : nodes.keySet()) {
            if ("UNVISITED".equals(status.get(nodeId)) && hasCycle(nodeId, status)) {
                errors.add("Flow에 순환 참조가 탐지되었습니다");
                break;
            }
        }
    }

    private boolean hasCycle(String nodeId, Map<String, String> status) {
        String currentStatus = status.get(nodeId);

        if ("VISITING".equals(currentStatus)) return true;
        if ("VISITED".equals(currentStatus)) return false;

        status.put(nodeId, "VISITING");

        AbstractNode node = nodes.get(nodeId);
        for (OutputPort outPort : node.getOutputPorts().values()) {
            for (Connection conn : outPort.getConnections()) {

                String nextNodeId = findTargetNodeId(conn);

                if (nextNodeId != null && hasCycle(nextNodeId, status)) {
                    return true;
                }
            }
        }

        status.put(nodeId, "VISITED");
        return false;
    }

    private String findTargetNodeId(Connection conn) {
        for (AbstractNode node : nodes.values()) {
            for (InputPort port : node.getInputPorts().values()) {
                if (((DefaultInputPort) port).getConnection() == conn) {
                    return node.getId();
                }
            }
        }
        return null;
    }

    public enum State {
        RUNNING, STOPPED
    }

}

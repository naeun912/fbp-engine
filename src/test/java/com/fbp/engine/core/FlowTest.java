package com.fbp.engine.core;

import com.fbp.engine.node.AbstractNode;
import com.fbp.engine.node.impl.PrintNode;
import com.fbp.engine.node.impl.SplitNode;
import com.fbp.engine.node.impl.TimerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowTest {
    Flow flow;
    Connection connection;

    @BeforeEach
    void setUp() {
        flow = new Flow("flow");
        connection = new Connection("connection - 1");
    }

    @Test
    @DisplayName("노드 등록")
    void createNodeTest() {
        PrintNode printNode = new PrintNode("printer");
        flow.addNode(printNode);

        assertNotNull(flow.getNodes().get("printer"));
    }

    @Test
    @DisplayName("메서드 체이닝")
    void methodChaining() {
        TimerNode timer = new TimerNode("timer", 1000);
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        PrintNode normalPrint = new PrintNode("mismatchPrint");

        assertDoesNotThrow(() -> {
            flow.addNode(timer)
                    .addNode(splitter)
                    .addNode(warningPrint)
                    .addNode(normalPrint)
                    .connect("timer", "out", "splitter", "in")
                    .connect("splitter", "match", "matchPrint", "in")
                    .connect("splitter", "mismatch", "mismatchPrint", "in");
        });

    }

    @Test
    @DisplayName("정상 연결")
    void connectionSuccess() {
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        assertEquals(0, flow.getConnections().size());
        flow.addNode(splitter)
                .addNode(warningPrint)
                .connect("splitter", "match", "matchPrint", "in");

        assertEquals(1, flow.getConnections().size());
    }

    @Test
    @DisplayName("존재하지 않는 소스 노드ID")
    void notSourceNodeIdTest() {
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        assertThrows(IllegalArgumentException.class, () -> {
            flow.addNode(splitter)
                    .addNode(warningPrint)
                    .connect("splitt", "match", "matchPrint", "in");
        });
    }

    @Test
    @DisplayName("존재하지 않는 대상 노드")
    void notTargetNodeIdTest() {
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        assertThrows(IllegalArgumentException.class, () -> {
            flow.addNode(splitter)
                    .addNode(warningPrint)
                    .connect("splitter", "match", "match", "in");
        });
    }

    @Test
    @DisplayName("존재하지 않는 소스 포트")
    void notSourceNodePostTest() {
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        assertThrows(IllegalArgumentException.class, () -> {
            flow.addNode(splitter)
                    .addNode(warningPrint)
                    .connect("splitter", "out", "matchPrint", "in");
        });
    }

    @Test
    @DisplayName("존재하지 않는 대상 포트")
    void notTargetNodePostTest() {
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        assertThrows(IllegalArgumentException.class, () -> {
            flow.addNode(splitter)
                    .addNode(warningPrint)
                    .connect("splitter", "match", "matchPrint", "out");
        });
    }

    @Test
    @DisplayName("validate - 빈 Flow")
    void emptyFlowTest() {
        List<String> errors = flow.validate();

        assertFalse(errors.isEmpty());

        assertTrue(errors.contains("Flow에 등록된 노드가 없습니다."));
    }

    @Test
    @DisplayName("validate - 정상 Flow")
    void normalFlowTest() {
        TimerNode timer = new TimerNode("timer", 1000);
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        PrintNode normalPrint = new PrintNode("mismatchPrint");

        flow.addNode(timer)
                .addNode(splitter)
                .addNode(warningPrint)
                .addNode(normalPrint)
                .connect("timer", "out", "splitter", "in")
                .connect("splitter", "match", "matchPrint", "in")
                .connect("splitter", "mismatch", "mismatchPrint", "in");

        List<String> errors = flow.validate();

        assertEquals(0, errors.size());
    }

    @Test
    @DisplayName("initialize - 전체 호출")
    void initializeAllTest() {
        TimerNode timerNode = spy(new TimerNode("timer", 1000));
        flow.addNode(timerNode);

        flow.initialize();

        verify(timerNode, times(1)).initialize();

    }

    @Test
    @DisplayName("shutdown - 전체 호출")
    void shutDownAllTest() {
        TimerNode timerNode = spy(new TimerNode("timer", 1000));
        flow.addNode(timerNode);

        flow.shutdown();

        verify(timerNode, times(1)).shutdown();
    }

    @Test
    @DisplayName("순환 참조 탐지 (도전)")
    void cycleTest() {
        AbstractNode nodeA = new SplitNode("nodeA", "tick", 1);
        AbstractNode nodeB = new SplitNode("nodeB", "tick", 1);
        AbstractNode dummy = new PrintNode("printer");

        flow.addNode(nodeA).addNode(nodeB).addNode(dummy);

        flow.connect("nodeA", "match", "nodeB", "in")
                .connect("nodeB", "match", "nodeA", "in");

        flow.connect("nodeA", "mismatch", "printer", "in")
                .connect("nodeB", "mismatch", "printer", "in");

        List<String> errors = flow.validate();

        assertFalse(errors.isEmpty());

        assertTrue(errors.stream().anyMatch(error -> error.contains("순환")));

    }

}
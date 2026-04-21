package com.fbp.engine.node.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MergeNodeTest {
    MergeNode mergeNode;
    FlowEngine engine;
    Flow flow;

    @BeforeEach
    void setUp() {
        flow = new Flow("flow");
        engine = new FlowEngine();
        mergeNode = new MergeNode("merge");
    }

    @Test
    @DisplayName("양쪽 입력 수신 & 합쳐진 메세지 출력")
    void bothInputPortTest() {
        TimerNode node1 = new TimerNode("t1", 1000);
        HumiditySensorNode node2 = new HumiditySensorNode("h1", 30, 90);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(mergeNode).addNode(dummy).addNode(node1).addNode(node2)
                .connect("t1", "out", "merge", "in-1")
                .connect("t1", "out", "h1", "trigger")
                .connect("h1", "out", "merge", "in-2")
                .connect("merge", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");
        Connection resultConn = flow.getConnections().get(2);
        Message mergedMsg = resultConn.poll();

        assertNotNull(mergedMsg);

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("한쪽만 도착 시 대기")
    void test() {
        TimerNode t1 = new TimerNode("t1", 1000);
        TimerNode silentNode = new TimerNode("silent", 999999);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(mergeNode).addNode(dummy).addNode(t1).addNode(silentNode)
                .connect("t1", "out", "merge", "in-1")
                .connect("silent", "out", "merge", "in-2")
                .connect("merge", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Connection resultConn = flow.getConnections().get(2);

        Message mergedMsg = null;
        try {
            mergedMsg = resultConn.poll(1500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNull(mergedMsg);
        engine.stopFlow("flow");

    }

    @Test
    @DisplayName("포트 구성 확인")
    void portNotNullTest() {
        TimerNode node1 = new TimerNode("t1", 1000);
        HumiditySensorNode node2 = new HumiditySensorNode("h1", 30, 90);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(mergeNode).addNode(dummy).addNode(node1).addNode(node2)
                .connect("t1", "out", "merge", "in-1")
                .connect("t1", "out", "h1", "trigger")
                .connect("h1", "out", "merge", "in-2")
                .connect("merge", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");
        assertNotNull(mergeNode.getOutputPort("out"));
        assertNotNull(mergeNode.getInputPort("in-1"));
        assertNotNull(mergeNode.getInputPort("in-2"));
        engine.stopFlow("flow");
    }

}
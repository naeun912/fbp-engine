package com.fbp.engine.node.rule;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.CollectorNode;
import com.fbp.engine.node.abstractImpl.DummySourceNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeWindowRuleNodeTest {
    FlowEngine engine;
    Flow flow;
    TimeWindowRuleNode windowNode;
    DummySourceNode sourceNode;
    CollectorNode alertCol;
    CollectorNode passCol;
    Message validMsg;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        flow = new Flow("flow");
        sourceNode = new DummySourceNode("source");
        windowNode = new TimeWindowRuleNode("window",
                m -> ((Number) m.get("val")).doubleValue() > 10.0, 1000, 2);

        alertCol = new CollectorNode("alert-col");
        passCol = new CollectorNode("pass-col");

        validMsg = new Message(Map.of("val", 20.0));

        flow.addNode(windowNode).addNode(sourceNode).addNode(alertCol).addNode(passCol);
        flow.connect("source", "out", "window", "in")
                .connect("window", "alert", "alert-col", "in")
                .connect("window", "pass", "pass-col", "in");

        engine.register(flow);
        engine.startFlow("flow");
    }

    @Test
    @DisplayName("기준 미달 → pass")
    void underThresholdTest() throws InterruptedException {
        sourceNode.onProcess(validMsg);

        Thread.sleep(500);
        assertEquals(1, passCol.getCollected().size());
        assertEquals(0, alertCol.getCollected().size());
    }

    @Test
    @DisplayName("기준 도달 → alert")
    void reachThresholdTest() throws InterruptedException {
        sourceNode.onProcess(validMsg);
        sourceNode.onProcess(validMsg);

        Thread.sleep(500);
        assertEquals(1, alertCol.getCollected().size());
    }

    @Test
    @DisplayName("시간 창 만료")
    void windowExpirationTest() throws InterruptedException {
        sourceNode.onProcess(validMsg);

        Thread.sleep(2000);

        sourceNode.onProcess(validMsg);

        Thread.sleep(500);

        assertEquals(2, passCol.getCollected().size());
        assertEquals(0, alertCol.getCollected().size());
    }

    @Test
    @DisplayName("조건 불만족 메시지")
    void invalidMessageFilteringTest() throws InterruptedException {
        Message invalidMsg = new Message(Map.of("val", 5.0));

        sourceNode.onProcess(invalidMsg);
        sourceNode.onProcess(validMsg);

        Thread.sleep(500);

        assertEquals(2, passCol.getCollected().size());
        assertEquals(0, alertCol.getCollected().size());
    }
}
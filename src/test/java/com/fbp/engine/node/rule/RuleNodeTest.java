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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RuleNodeTest {
    RuleNode ruleNode;
    Flow flow;
    FlowEngine engine;
    CollectorNode matchCollector;
    CollectorNode mismatchCollector;
    DummySourceNode dummySource;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        flow = new Flow("flow");

        ruleNode = new RuleNode("rule", (msg) -> {
            Double temp = (Double) msg.payload().get("temperature");
            return temp != null && temp > 30.0;
        });

        dummySource = new DummySourceNode("dummy-source");
        matchCollector = new CollectorNode("match-collector");
        mismatchCollector = new CollectorNode("mismatch-collector");

        flow.addNode(ruleNode)
                .addNode(matchCollector)
                .addNode(mismatchCollector)
                .addNode(dummySource);


        flow.connect("dummy-source", "out", "rule", "in")
                .connect("rule", "match", "match-collector", "in")
                .connect("rule", "mismatch", "mismatch-collector", "in");

        engine.register(flow);

    }

    @Test
    @DisplayName("조건 만족 → match")
    void testMatchCondition() throws InterruptedException {
        engine.startFlow("flow");

        Message m1 = new Message(Map.of("temperature", 35.0));
        ruleNode.onProcess(m1);

        Thread.sleep(100);

        assertEquals(1, matchCollector.getCollected().size());
        assertEquals(35.0, matchCollector.getCollected().getFirst().payload().get("temperature"));
    }

    @Test
    @DisplayName("조건 불만족 → mismatch")
    void testMismatchCondition() throws InterruptedException {
        engine.startFlow("flow");

        Message m2 = new Message(Map.of("temperature", 20.0));
        ruleNode.onProcess(m2);

        Thread.sleep(100);

        assertEquals(1, mismatchCollector.getCollected().size());
        assertEquals(20.0, mismatchCollector.getCollected().getFirst().payload().get("temperature"));
    }

    @Test
    @DisplayName("포트 구성")
    void portGetNotNullTest() {
        assertNotNull(ruleNode.getInputPort("in"));
        assertNotNull(ruleNode.getOutputPort("match"));
        assertNotNull(ruleNode.getOutputPort("mismatch"));
    }

    @Test
    @DisplayName("Null 처리 필드")
    void testMissingFieldHandling() throws InterruptedException {
        engine.startFlow("flow");

        Message m3 = new Message(Map.of("humidity", 85.5));
        ruleNode.onProcess(m3);

        Thread.sleep(100);

        assertEquals(1, mismatchCollector.getCollected().size());
    }

    @Test
    @DisplayName("다수 메세지 분기")
    void testMultipleMessagesBranching() throws InterruptedException {
        engine.startFlow("flow");

        ruleNode.onProcess(new Message(Map.of("temperature", 35.0)));
        ruleNode.onProcess(new Message(Map.of("temperature", 10.0)));
        ruleNode.onProcess(new Message(Map.of("temperature", 45.0)));
        ruleNode.onProcess(new Message(Map.of("temperature", 5.0)));

        Thread.sleep(200);

        assertEquals(2, matchCollector.getCollected().size());
        assertEquals(2, mismatchCollector.getCollected().size());

        assertEquals(35.0, matchCollector.getCollected().get(0).payload().get("temperature"));
        assertEquals(45.0, matchCollector.getCollected().get(1).payload().get("temperature"));
    }
}
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

class CompositeRuleNodeTest {
    Flow flow;
    FlowEngine engine;
    CollectorNode matchCol;
    CollectorNode mismatchCol;
    Message testMsg;
    DummySourceNode sourceNode;

    @BeforeEach
    void setUp() {
        matchCol = new CollectorNode("match-col");
        mismatchCol = new CollectorNode("mismatch-col");
        testMsg = new Message(Map.of("temp", 35.0, "status", "ON"));
        flow = new Flow("flow");
        engine = new FlowEngine();
        sourceNode = new DummySourceNode("source-node");
    }

    @Test
    @DisplayName("AND — 모두 만족")
    void AndTrueMatchTest() throws InterruptedException {
        CompositeRuleNode node = new CompositeRuleNode("node", Operator.AND);
        node.addCondition("temp", ">", 30.0);
        node.addCondition("status", "==", "ON");
        DummySourceNode sourceNode = new DummySourceNode("source-node");
        flow.addNode(matchCol).addNode(mismatchCol).addNode(sourceNode).addNode(node);
        flow.connect(sourceNode.getId(), "out", node.getId(), "in")
                .connect(node.getId(), "match", matchCol.getId(), "in")
                .connect(node.getId(), "mismatch", mismatchCol.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");
        sourceNode.onProcess(testMsg);
        Thread.sleep(1000);
        assertEquals(1, matchCol.getCollected().size());
    }

    @Test
    @DisplayName("AND — 하나 불만족")
    void AndFalseMismatchTest() throws InterruptedException {
        CompositeRuleNode node = new CompositeRuleNode("node", Operator.AND);
        node.addCondition("temp", ">", 36.0);
        node.addCondition("status", "==", "ON");
        DummySourceNode sourceNode = new DummySourceNode("source-node");
        flow.addNode(matchCol).addNode(mismatchCol).addNode(sourceNode).addNode(node);

        flow.connect(sourceNode.getId(), "out", node.getId(), "in")
                .connect(node.getId(), "match", matchCol.getId(), "in")
                .connect(node.getId(), "mismatch", mismatchCol.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");
        sourceNode.onProcess(testMsg);
        Thread.sleep(1000);
        assertEquals(1, mismatchCol.getCollected().size());
    }

    @Test
    @DisplayName("OR — 하나 만족")
    void OrTrueMatchTest() throws InterruptedException {
        CompositeRuleNode node = new CompositeRuleNode("node", Operator.OR);
        node.addCondition("temp", ">", 36.0);
        node.addCondition("status", "==", "ON");
        DummySourceNode sourceNode = new DummySourceNode("source-node");
        flow.addNode(matchCol).addNode(mismatchCol).addNode(sourceNode).addNode(node);

        flow.connect(sourceNode.getId(), "out", node.getId(), "in")
                .connect(node.getId(), "match", matchCol.getId(), "in")
                .connect(node.getId(), "mismatch", mismatchCol.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");
        sourceNode.onProcess(testMsg);
        Thread.sleep(1000);
        assertEquals(1, matchCol.getCollected().size());
    }

    @Test
    @DisplayName("OR — 모두 불만족")
    void OrFalseMismatchTest() throws InterruptedException {
        CompositeRuleNode node = new CompositeRuleNode("node", Operator.OR);
        node.addCondition("temp", ">", 36.0);
        node.addCondition("status", "!=", "ON");
        DummySourceNode sourceNode = new DummySourceNode("source-node");
        flow.addNode(matchCol).addNode(mismatchCol).addNode(sourceNode).addNode(node);

        flow.connect(sourceNode.getId(), "out", node.getId(), "in")
                .connect(node.getId(), "match", matchCol.getId(), "in")
                .connect(node.getId(), "mismatch", mismatchCol.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");
        sourceNode.onProcess(testMsg);
        Thread.sleep(1000);
        assertEquals(1, mismatchCol.getCollected().size());
    }

    @Test
    @DisplayName("빈 조건")
    void EmptyConditionTest() throws InterruptedException {
        CollectorNode andMatchCol = new CollectorNode("and-match");
        CollectorNode andMismatchCol = new CollectorNode("and-mismatch");
        CollectorNode orMatchCol = new CollectorNode("or-match");
        CollectorNode orMismatchCol = new CollectorNode("or-mismatch");

        flow.addNode(andMatchCol).addNode(andMismatchCol)
                .addNode(orMatchCol).addNode(orMismatchCol).addNode(sourceNode);

        CompositeRuleNode andNode = new CompositeRuleNode("and", Operator.AND);
        flow.addNode(andNode);
        flow.connect("source-node", "out", "and", "in");
        flow.connect("and", "match", "and-match", "in");
        flow.connect("and", "mismatch", "and-mismatch", "in");

        CompositeRuleNode orNode = new CompositeRuleNode("or", Operator.OR);
        flow.addNode(orNode);
        flow.connect("source-node", "out", "or", "in");
        flow.connect("or", "match", "or-match", "in");
        flow.connect("or", "mismatch", "or-mismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        sourceNode.onProcess(testMsg);
        Thread.sleep(100);

        assertEquals(1, andMatchCol.getCollected().size());
        assertEquals(0, andMismatchCol.getCollected().size());

        assertEquals(0, orMatchCol.getCollected().size());
        assertEquals(1, orMismatchCol.getCollected().size());
    }
}
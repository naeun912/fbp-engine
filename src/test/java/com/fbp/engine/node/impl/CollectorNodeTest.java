package com.fbp.engine.node.impl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectorNodeTest {
    Flow flow;
    FlowEngine engine;
    CollectorNode collector;
    TimerNode timer;

    @BeforeEach
    void setUp() {
        collector = new CollectorNode("collector");
        flow = new Flow("flow");
        engine = new FlowEngine();
        timer = new TimerNode("timer", 1000);
    }

    @Test
    @DisplayName("메세지 수집")
    void messageSaveTest() throws InterruptedException {
        flow.addNode(timer).addNode(collector);

        flow.connect("timer", "out", "collector", "in");
        engine.register(flow);
        engine.startFlow("flow");
        Thread.sleep(3000);
        engine.stopFlow("flow");

        assertNotNull(collector.getCollected());
        System.out.println(collector.getCollected());
    }

    @Test
    @DisplayName("수집 순서 보존")
    void messageListOrderTest() throws InterruptedException {
        flow.addNode(timer).addNode(collector);

        flow.connect("timer", "out", "collector", "in");
        engine.register(flow);
        engine.startFlow("flow");
        Thread.sleep(3000);
        engine.stopFlow("flow");

        assertEquals(1, (int) collector.getCollected().getFirst().get("tick"));
        assertEquals(2, (int) collector.getCollected().get(1).get("tick"));
        assertEquals(3, (int) collector.getCollected().get(2).get("tick"));
    }

    @Test
    @DisplayName("초기 상태 빈 리스트")
    void emptyListTest() throws InterruptedException {
        flow.addNode(timer).addNode(collector);

        flow.connect("timer", "out", "collector", "in");
        engine.register(flow);
        engine.startFlow("flow");
        assertTrue(collector.getCollected().isEmpty());
        Thread.sleep(3000);
        engine.stopFlow("flow");

        System.out.println(collector.getCollected());
    }

    @Test
    @DisplayName("InputPort 존재")
    void inputPortTest() {
        assertNotNull(collector.getInputPort("in"));
    }

    @Test
    @DisplayName("파이프라인 연결 검증")
    void generatorNodeTest() throws InterruptedException {
        GeneratorNode generatorNode = new GeneratorNode("generate");
        flow.addNode(generatorNode).addNode(collector);

        flow.connect("generate", "out", "collector", "in");

        engine.register(flow);

        engine.startFlow("flow");
        generatorNode.generate("hihi", "hello~~~");
        generatorNode.generate("hihi", "hello~~~");
        generatorNode.generate("hihi", "hello~~~");
        Thread.sleep(3000);
        engine.stopFlow("flow");

        assertEquals("hello~~~", collector.getCollected().getFirst().get("hihi"));
    }
}
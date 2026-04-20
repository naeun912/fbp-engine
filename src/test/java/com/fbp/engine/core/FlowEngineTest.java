package com.fbp.engine.core;

import com.fbp.engine.node.impl.DelayNode;
import com.fbp.engine.node.impl.PrintNode;
import com.fbp.engine.node.impl.TimerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlowEngineTest {
    Flow flow;
    FlowEngine flowEngine;

    @BeforeEach
    void setUp() {
        flow = new Flow("flow");
        flowEngine = new FlowEngine();
    }

    @Test
    @DisplayName("초기 상태")
    void firstFlowTest() {
        assertEquals(FlowEngine.State.INITIALIZED, flowEngine.getState());
    }

    @Test
    @DisplayName("플로우 등록")
    void flowRegisterTest() {
        flowEngine.register(flow);

        assertEquals(flow.getId(), flowEngine.getFlows().get("flow").getId());
    }

    @Test
    @DisplayName("startFlow 정상")
    void normalStartFlowTest() {
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printer");
        DelayNode delay = new DelayNode("delay", 1000);

        flow.addNode(printer)
                .addNode(delay)
                .addNode(timerNode)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printer", "in");
        flowEngine.register(flow);

        flowEngine.startFlow("flow");
        assertEquals(FlowEngine.State.RUNNING, flowEngine.getState());
        assertEquals(Flow.State.RUNNING, flow.getState());
    }

    @Test
    @DisplayName("startFlow - 없는 ID")
    void NotExistIdTest() {
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printer");
        DelayNode delay = new DelayNode("delay", 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            flow.addNode(printer)
                    .addNode(delay)
                    .addNode(timerNode)
                    .connect("timer", "out", "delay", "in")
                    .connect("delay", "out", "printer", "in")
                    .connect("dd", "out", "f[", "in");
        });
        flowEngine.register(flow);

        flowEngine.startFlow("flow");
    }

    @Test
    @DisplayName("startFlow - 유효성 실패")
    void validateFailedTest() {
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printer");
        DelayNode delay = new DelayNode("delay", 1000);


        flow.addNode(printer)
                .addNode(delay)
                .addNode(timerNode);
        flowEngine.register(flow);

        assertThrows(IllegalStateException.class, () -> {
            flowEngine.startFlow("flow");
        });
    }

    @Test
    @DisplayName("stopFlow 정상")
    void normalStopFlowTest() {
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printer");
        DelayNode delay = new DelayNode("delay", 1000);

        flow.addNode(printer)
                .addNode(delay)
                .addNode(timerNode)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printer", "in");
        flowEngine.register(flow);

        flowEngine.startFlow("flow");

        flowEngine.stopFlow("flow");
        assertEquals(Flow.State.STOPPED, flow.getState());
    }

    @Test
    @DisplayName("shotDown 전체")
    void shotDownAllTest() {
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printer");
        DelayNode delay = new DelayNode("delay", 1000);

        flow.addNode(printer)
                .addNode(delay)
                .addNode(timerNode)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printer", "in");
        flowEngine.register(flow);

        flowEngine.startFlow("flow");
        flowEngine.stopFlow("flow");
        assertEquals(Flow.State.STOPPED, flow.getState());
    }

    @Test
    @DisplayName("다중 플로우 독립 동작")
    void multiFlowTest() {
        Flow flowA = new Flow("flowA");
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printerA");
        DelayNode delay = new DelayNode("delay", 1000);

        flowA.addNode(printer)
                .addNode(delay)
                .addNode(timerNode)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printerA", "in");

        Flow flowB = new Flow("flowB");
        TimerNode timerNodeB = new TimerNode("timer", 3000);
        PrintNode printerB = new PrintNode("printerB");
        DelayNode delayB = new DelayNode("delay", 1000);

        flowB.addNode(printerB)
                .addNode(delayB)
                .addNode(timerNodeB)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printerB", "in");

        flowEngine.register(flowA);
        flowEngine.register(flowB);

        flowEngine.startFlow("flowA");
        flowEngine.startFlow("flowB");

        flowEngine.stopFlow("flowA");

        assertEquals(Flow.State.STOPPED, flowA.getState());
        assertEquals(Flow.State.RUNNING, flowB.getState());
        assertEquals(FlowEngine.State.RUNNING, flowEngine.getState());
    }

    @Test
    @DisplayName("listFlows 출력")
    void listFlowsTest() {
        Flow flowA = new Flow("flowA");
        TimerNode timerNode = new TimerNode("timer", 3000);
        PrintNode printer = new PrintNode("printerA");
        DelayNode delay = new DelayNode("delay", 1000);

        flowA.addNode(printer)
                .addNode(delay)
                .addNode(timerNode)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printerA", "in");

        Flow flowB = new Flow("flowB");
        TimerNode timerNodeB = new TimerNode("timer", 3000);
        PrintNode printerB = new PrintNode("printerB");
        DelayNode delayB = new DelayNode("delay", 1000);

        flowB.addNode(printerB)
                .addNode(delayB)
                .addNode(timerNodeB)
                .connect("timer", "out", "delay", "in")
                .connect("delay", "out", "printerB", "in");

        flowEngine.register(flowA);
        flowEngine.register(flowB);

        flowEngine.startFlow("flowA");
        flowEngine.startFlow("flowB");

        flowEngine.stopFlow("flowA");

        flowEngine.listFlows();
    }
}
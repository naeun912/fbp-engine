package com.fbp.engine.node.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdFilterNodeTest {
    ThresholdFilterNode filterNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        filterNode = new ThresholdFilterNode("threshold", "temperature", 30);
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("초과 → alert 포트")
    void alertPortTest() throws InterruptedException {
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode temperatureSensorNode = new TemperatureSensorNode("temp", 31, 45);
        ThresholdFilterNode thresholdFilterNode = new ThresholdFilterNode("threshold", "temperature", 30);
        AlertNode alertNode = new AlertNode("alert");
        PrintNode printNode = new PrintNode("printer");

        flow.addNode(timerNode)
                .addNode(temperatureSensorNode)
                .addNode(thresholdFilterNode)
                .addNode(alertNode)
                .addNode(printNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "printer", "in");
        engine.register(flow);
        engine.startFlow("flow");
        Connection alertConn = flow.getConnections().get(2);

        Message alertMsg = alertConn.poll(2000, TimeUnit.SECONDS);

        assertNotNull(alertMsg);

        double tempValue = alertMsg.get("temperature");
        assertTrue(tempValue > 30);

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("이하 → normal 포트")
    void normalPortTest() throws InterruptedException {
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode temperatureSensorNode = new TemperatureSensorNode("temp", 10, 29);
        ThresholdFilterNode thresholdFilterNode = new ThresholdFilterNode("threshold", "temperature", 30);
        AlertNode alertNode = new AlertNode("alert");
        PrintNode printNode = new PrintNode("printer");

        flow.addNode(timerNode)
                .addNode(temperatureSensorNode)
                .addNode(thresholdFilterNode)
                .addNode(alertNode)
                .addNode(printNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "printer", "in");
        engine.register(flow);
        engine.startFlow("flow");
        Connection normalConn = flow.getConnections().get(3);

        Message normalMsg = normalConn.poll(2000, TimeUnit.SECONDS);

        assertNotNull(normalMsg);

        double tempValue = normalMsg.get("temperature");
        assertTrue(tempValue < 30);

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("경계값 (정확히 같은 값)")
    void normalPortTest2() throws InterruptedException {
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode temperatureSensorNode = new TemperatureSensorNode("temp", 30, 30);
        ThresholdFilterNode thresholdFilterNode = new ThresholdFilterNode("threshold", "temperature", 30);
        AlertNode alertNode = new AlertNode("alert");
        PrintNode printNode = new PrintNode("printer");

        flow.addNode(timerNode)
                .addNode(temperatureSensorNode)
                .addNode(thresholdFilterNode)
                .addNode(alertNode)
                .addNode(printNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "printer", "in");
        engine.register(flow);
        engine.startFlow("flow");
        Connection normalConn = flow.getConnections().get(3);

        Message normalMsg = normalConn.poll(2000, TimeUnit.SECONDS);

        assertNotNull(normalMsg);

        double tempValue = normalMsg.get("temperature");
        assertEquals(30, tempValue);

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("키 없는 메세지")
    void noKeyMessageTest() {
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("other_key", "hello");
        Message ghostMessage = new Message(emptyData);
        TimerNode timerNode = new TimerNode("timer", 1000);
        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(timerNode).addNode(filterNode).addNode(dummy)
                .connect("timer", "out", "filter", "in")
                .connect("filter", "normal", "dummy", "in")
                .connect("filter", "alert", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");

        assertDoesNotThrow(() -> {
            filterNode.onProcess(ghostMessage);
        });

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("양쪽 동시 검증")
    void bothInputPortTest() throws InterruptedException {
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode tempNode = new TemperatureSensorNode("temp", 25, 35);
        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30);

        CollectorNode alertCollector = new CollectorNode("alertCollector");
        CollectorNode normalCollector = new CollectorNode("normalCollector");

        flow.addNode(timerNode).addNode(tempNode).addNode(filterNode)
                .addNode(alertCollector).addNode(normalCollector)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "filter", "in")
                .connect("filter", "alert", "alertCollector", "in")
                .connect("filter", "normal", "normalCollector", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Thread.sleep(3000);
        engine.stopFlow("flow");

        for (Message msg : alertCollector.getCollected()) {
            double val = msg.get("temperature");
            assertTrue(val > 30);
        }

        for (Message msg : normalCollector.getCollected()) {
            double val = msg.get("temperature");
            assertTrue(val <= 30);
        }
    }
}
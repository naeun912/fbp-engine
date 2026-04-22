package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.impl.CollectorNode;
import com.fbp.engine.node.impl.TemperatureSensorNode;
import com.fbp.engine.node.impl.ThresholdFilterNode;
import com.fbp.engine.node.impl.TimerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureMainFlowTest {
    TemperatureSensorNode temperatureSensorNode;
    Flow flow;
    FlowEngine engine;
    CollectorNode alertCollector;
    CollectorNode normalCollector;
    ThresholdFilterNode filterNode;
    TimerNode timer;

    @BeforeEach
    void setUp() {
        temperatureSensorNode = new TemperatureSensorNode("temp", 15, 45);
        filterNode = new ThresholdFilterNode("filter", "temperature", 30);
        alertCollector = new CollectorNode("alertCollector");
        normalCollector = new CollectorNode("normalCollector");
        flow = new Flow("flow");
        engine = new FlowEngine();
        timer = new TimerNode("timer", 1000);

        flow.addNode(timer).addNode(temperatureSensorNode).addNode(filterNode)
                .addNode(alertCollector).addNode(normalCollector);

        flow.connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "filter", "in")
                .connect("filter", "alert", "alertCollector", "in")
                .connect("filter", "normal", "normalCollector", "in");
    }

    @Test
    @DisplayName("alert 경로 검증")
    void alertTest() throws InterruptedException {
        engine.register(flow);
        engine.startFlow("flow");
        Thread.sleep(3000);
        engine.stopFlow("flow");

        List<Message> alertList = alertCollector.getCollected();

        assertFalse(alertList.isEmpty());
        for (Message msg : alertList) {
            double temp = msg.get("temperature");
            assertTrue(temp > filterNode.getThreshold());
        }
    }

    @Test
    @DisplayName("normal 경로 검증")
    void normalTest() throws InterruptedException {
        engine.register(flow);
        engine.startFlow("flow");
        Thread.sleep(3000);
        engine.stopFlow("flow");

        List<Message> normalList = normalCollector.getCollected();

        assertFalse(normalList.isEmpty());
        for (Message msg : normalList) {
            double temp = msg.get("temperature");
            assertTrue(temp <= filterNode.getThreshold());
        }
    }

    @Test
    @DisplayName("전체 메시지 수")
    void totalCountTest() throws InterruptedException {
        engine.register(flow);
        engine.startFlow("flow");

        Thread.sleep(2200);
        engine.stopFlow("flow");

        int alertCount = alertCollector.getCollected().size();
        int normalCount = normalCollector.getCollected().size();

        int totalCollected = alertCount + normalCount;

        System.out.println("Alert: " + alertCount + ", Normal: " + normalCount);
        System.out.println("Total: " + totalCollected);

        assertTrue(totalCollected > 0);
    }
}

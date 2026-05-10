package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureSensorNodeTest {
    TemperatureSensorNode temperatureSensorNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        temperatureSensorNode = new TemperatureSensorNode("temp", 15, 45);
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("온도 범위 확인")
    void temperatureTest() {
        TimerNode timerNode = new TimerNode("timer", 1000);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(temperatureSensorNode).addNode(dummy).addNode(timerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");


        Connection conn = flow.getConnections().get(1);
        Message msg = conn.poll();

        assertNotNull(msg);
        double temp = msg.get("temperature");

        assertTrue(temp >= 15 && temp <= 45);

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("필수 키 포함")
    void containKeyTest() {
        TimerNode timerNode = new TimerNode("timer", 1000);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(temperatureSensorNode).addNode(dummy).addNode(timerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Connection conn = flow.getConnections().get(1);
        Message msg = conn.poll();

        assertNotNull(msg);
        assertTrue(msg.hasKey("sensorId"));
        assertTrue(msg.hasKey("temperature"));
        assertTrue(msg.hasKey("unit"));
        assertTrue(msg.hasKey("timestamp"));

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("sensorId 일치")
    void sensorIdTrueTest() {
        TimerNode timerNode = new TimerNode("timer", 1000);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(temperatureSensorNode).addNode(dummy).addNode(timerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Connection conn = flow.getConnections().get(1);
        Message msg = conn.poll();

        assertNotNull(msg);
        assertEquals("temp", msg.get("sensorId"));

        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("트리거마다 생성")
    void triggerTest() {
        TimerNode timerNode = new TimerNode("timer", 1000);
        PrintNode dummy = new PrintNode("dummy");

        flow.addNode(temperatureSensorNode).addNode(dummy).addNode(timerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "dummy", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Connection conn = flow.getConnections().get(1);

        assertNotNull(conn.poll());
        assertNotNull(conn.poll());

        engine.stopFlow("flow");

    }

}
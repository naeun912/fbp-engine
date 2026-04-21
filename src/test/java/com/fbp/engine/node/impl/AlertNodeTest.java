package com.fbp.engine.node.impl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AlertNodeTest {
    AlertNode alertNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        alertNode = new AlertNode("alert");
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("정상처리")
    void normalTest() throws InterruptedException {
        TemperatureSensorNode tempNode = new TemperatureSensorNode("temp", 31, 40);
        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30);
        CollectorNode collector = new CollectorNode("collector");

        flow.addNode(tempNode).addNode(filterNode).addNode(alertNode).addNode(collector)
                .connect("temp", "out", "filter", "in")
                .connect("filter", "alert", "alert", "in")
                .connect("alert", "out", "collector", "in");

        engine.register(flow);
        engine.startFlow("flow");

        // 2초 정도 흐르게 둠
        Thread.sleep(2000);
        engine.stopFlow("flow");

        // 💡 검증: 컬렉터에 메시지가 쌓였나?
        assertFalse(collector.getCollected().isEmpty(), "AlertNode를 거쳐온 메시지가 하나도 없습니다!");

        // 첫 번째 메시지에 필수 키가 다 있는지 확인
        Message m = collector.getCollected().getFirst();
        assertNotNull(m.get("sensorId"));
        assertNotNull(m.get("temperature"));
    }

}
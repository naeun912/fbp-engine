package com.fbp.engine.node.protocolImpl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MqttPublisherNodeTest {
    MqttPublisherNode mqttPublisherNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        Map<String, Object> pubConfig = new HashMap<>();
        pubConfig.put("brokerUrl", "tcp://localhost:1883");
        pubConfig.put("clientId", "naeun-pub-alert");
        pubConfig.put("topic", "alert/temp");
        mqttPublisherNode = new MqttPublisherNode("pub", pubConfig);
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("포트 구성")
    void portNotNullTest() {
        assertNotNull(mqttPublisherNode.getInputPort("in"));
    }

    @Test
    @DisplayName("초기 상태")
    void isConnectedFalseTest() {
        assertFalse(mqttPublisherNode.isConnected());
    }

    @Test
    @DisplayName("config 기본 토픽 조회")
    void getConfigTest() {
        assertEquals("alert/temp", mqttPublisherNode.getConfig("topic"));
    }

}
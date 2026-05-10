package com.fbp.engine.node.protocolImpl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MqttSubscriberNodeTest {
    MqttSubscriberNode mqttSubscriberNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        Map<String, Object> subConfig = new HashMap<>();
        subConfig.put("brokerUrl", "tcp://localhost:1883");
        subConfig.put("clientId", "naeun-sub-temp");
        subConfig.put("topic", "sensor/temp");
        mqttSubscriberNode = new MqttSubscriberNode("sub", subConfig);
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("포트 구성")
    void portNotNullTest() {
        assertNotNull(mqttSubscriberNode.getOutputPort("out"));
    }

    @Test
    @DisplayName("초기 상태")
    void isConnectedFalseTest() {
        assertFalse(mqttSubscriberNode.isConnected());
    }

    @Test
    @DisplayName("config 조회")
    void getConfigTest() {
        assertEquals("tcp://localhost:1883", mqttSubscriberNode.getConfig("brokerUrl"));
    }

    @Test
    @DisplayName("JSON → Message 변환")
    void jsonMessageChangeTest() {
        String json = "{\"temperature\": 35.5, \"status\": \"ok\"}";

        MqttMessage mockMsg = new MqttMessage(json.getBytes());

        assertDoesNotThrow(() -> {
            mqttSubscriberNode.processMessage("sensor/temp", mockMsg);
        });
    }

    @Test
    @DisplayName("JSON 파싱 실패 처리")
    void jsonParsingFailTest() {
        String invalidJson = "This is not JSON!!!";
        MqttMessage mockMsg = new MqttMessage(invalidJson.getBytes());

        assertDoesNotThrow(() -> {
            mqttSubscriberNode.processMessage("sensor/temp", mockMsg);
        });
    }


}
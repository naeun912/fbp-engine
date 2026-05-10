package com.fbp.engine.node.protocolImpl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class MqttSubscriberNodeAllTest {
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

    @AfterEach
    void tearDown() {
        mqttSubscriberNode.disconnect();
    }

    @Test
    @DisplayName("Broker 연결 성공")
    void brokerConnectionTest() throws IOException {
        mqttSubscriberNode.initialize();

        assertTrue(mqttSubscriberNode.isConnected());
    }

    @Test
    @DisplayName("메시지 수신 & 토픽 정보 포함")
    void messageReceiveAndTopicTest() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message> resultMsg = new AtomicReference<>();

        AbstractNode collector = new AbstractNode("collector") {
            @Override
            public void onProcess(Message message) {
                resultMsg.set(message);
                latch.countDown();
            }
        };
        collector.addInputPort("in");

        flow.addNode(mqttSubscriberNode)
                .addNode(collector)
                .connect(mqttSubscriberNode.getId(), "out", collector.getId(), "in");

        engine.register(flow);
        engine.startFlow("flow");

        mqttSubscriberNode.connect();

        MqttClient testClient =
                new MqttClient("tcp://localhost:1883", "test-pub-" + System.currentTimeMillis());

        try {
            testClient.connect();
            String payload = "{\"temperature\": 25.5}";
            testClient.publish("sensor/temp", new MqttMessage(payload.getBytes()));
        } finally {
            if (testClient.isConnected()) {
                testClient.disconnect();
            }
            testClient.close();
        }

        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));

        assertEquals("sensor/temp", resultMsg.get().get("topic"));
        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    void shutdownDisconnectTest() throws IOException {
        mqttSubscriberNode.initialize();
        assertTrue(mqttSubscriberNode.isConnected());

        mqttSubscriberNode.shutdown();

        assertFalse(mqttSubscriberNode.isConnected());
    }
}
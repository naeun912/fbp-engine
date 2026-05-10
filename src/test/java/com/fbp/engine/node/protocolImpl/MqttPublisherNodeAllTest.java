package com.fbp.engine.node.protocolImpl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class MqttPublisherNodeAllTest {
    MqttPublisherNode mqttPublisherNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        Map<String, Object> pubConfig = new HashMap<>();
        pubConfig.put("brokerUrl", "tcp://localhost:1883");
        pubConfig.put("clientId", "naeun-pub-test" + System.currentTimeMillis());
        pubConfig.put("topic", "alert/temp");
        mqttPublisherNode = new MqttPublisherNode("pub", pubConfig);
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @AfterEach
    void tearDown() {
        mqttPublisherNode.disconnect();
    }

    @Test
    @DisplayName("Broker 연결 성공")
    void brokerConnectionTest() {
        mqttPublisherNode.initialize();

        assertTrue(mqttPublisherNode.isConnected());
    }

    @Test
    @DisplayName("메시지 수신 & 동적 토픽")
    void messageReceiveAndTopicTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedTopic = new AtomicReference<>();

        // ✅ MqttClient 대신 MqttAsyncClient를 선언합니다.
        MqttAsyncClient checkClient = new MqttAsyncClient("tcp://localhost:1883", "check-sub");

        checkClient.connect().waitForCompletion();

        checkClient.setCallback(new org.eclipse.paho.mqttv5.client.MqttCallback() {
            @Override
            public void messageArrived(String topic, org.eclipse.paho.mqttv5.common.MqttMessage message) {
                receivedTopic.set(topic);
                latch.countDown();
            }

            @Override
            public void disconnected(org.eclipse.paho.mqttv5.client.MqttDisconnectResponse res) {
            }

            @Override
            public void mqttErrorOccurred(org.eclipse.paho.mqttv5.common.MqttException e) {
            }

            @Override
            public void deliveryComplete(org.eclipse.paho.mqttv5.client.IMqttToken token) {
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
            }

            @Override
            public void authPacketArrived(int i, MqttProperties mqttProperties) {
            }
        });

        checkClient.subscribe("#", 1).waitForCompletion();

        AbstractNode source = new AbstractNode("source") {
            @Override
            public void onProcess(Message msg) {
            }
        };
        source.addOutputPort("out");
        flow.addNode(source).addNode(mqttPublisherNode);
        flow.connect(source.getId(), "out", mqttPublisherNode.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");

        // 과제 명세서 조건: "topic" 키가 있으면 해당 토픽으로 발행
        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", "dynamic/alert");
        payload.put("data", "fire!");
        com.fbp.engine.message.Message testMsg = new com.fbp.engine.message.Message(payload);

        mqttPublisherNode.process(testMsg);

        // 검증
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS), "메시지가 전송되지 않았습니다.");
        assertEquals("dynamic/alert", receivedTopic.get());

        checkClient.disconnect();
        checkClient.close();
        engine.stopFlow("flow");
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    void shutdownDisconnectTest() {
        mqttPublisherNode.initialize();
        assertTrue(mqttPublisherNode.isConnected());

        mqttPublisherNode.shutdown();

        assertFalse(mqttPublisherNode.isConnected());
    }
}

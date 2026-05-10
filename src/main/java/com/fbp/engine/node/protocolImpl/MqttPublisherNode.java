package com.fbp.engine.node.protocolImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.ProtocolNode;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.io.IOException;
import java.util.Map;

public class MqttPublisherNode extends ProtocolNode {
    private MqttClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MqttPublisherNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
    }

    @Override
    public void connect() throws IOException {

        try {
            String brokerUrl = (String) config.get("brokerUrl");
            String clientId = (String) config.get("clientId");
            String topic = (String) config.get("topic");
            int qos = (int) config.getOrDefault("qos", 1);
            boolean retained = (boolean) config.getOrDefault("retained", false);

            client = new MqttClient(brokerUrl, clientId);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);
            System.out.println("✅ Publisher 연결 성공: " + brokerUrl);
        } catch (MqttException e) {
            throw new IOException("MQTT Publisher 연결 실패", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProcess(Message message) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(message.payload());


            String topic = (String) message.payload().get("topic");

            if (topic == null || topic.isEmpty()) {
                topic = (String) config.get("topic");
            }

            if (topic == null || topic.isEmpty()) {
                System.err.println("⚠️ 발행 실패: 토픽 정보가 메시지나 설정(config)에 없습니다.");
                return;
            }
            int qos = (int) config.getOrDefault("qos", 1);
            boolean retained = (boolean) config.getOrDefault("retained", false);

            MqttMessage mqttMessage = new MqttMessage(jsonPayload.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            if (client != null && client.isConnected()) {
                client.publish(topic, mqttMessage);
            }
        } catch (Exception e) {
            System.err.println("❌ 발행 실패: " + e.getMessage());
            throw new RuntimeException("MQTT Publish 도중 에러 발생", e);
        }

    }

    @Override
    protected byte[] readData() throws IOException {
        return null;
    }
}

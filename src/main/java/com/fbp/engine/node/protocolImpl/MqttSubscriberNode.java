package com.fbp.engine.node.protocolImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.ProtocolNode;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MqttSubscriberNode extends ProtocolNode {
    private MqttClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubscriberNode(String id, Map<String, Object> config) {
        super(id, config);
        addOutputPort("out");
    }


    @Override
    public void connect() throws IOException {
        try {
            String brokerUrl = (String) config.get("brokerUrl");
            String clientId = (String) config.get("clientId");
            String topic = (String) config.get("topic");
            int qos = (int) config.getOrDefault("qos", 1);

            client = new MqttClient(brokerUrl, clientId);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);

            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    processMessage(topic, message);
                }

                // 아래는 인터페이스 때문에 강제로 구현해야 하는 빈 메서드들 (그냥 두세요)
                @Override
                public void disconnected(MqttDisconnectResponse dr) {
                }

                @Override
                public void mqttErrorOccurred(MqttException e) {
                }

                @Override
                public void deliveryComplete(IMqttToken t) {
                }

                @Override
                public void connectComplete(boolean r, String u) {
                }

                @Override
                public void authPacketArrived(int rc, MqttProperties p) {
                }
            });

            client.subscribe(topic, qos);
            System.out.println("✅ [" + getId() + "] MQTT 구독 성공: " + topic);
        } catch (MqttException e) {
            throw new IOException("MQTT 연결 실패!", e);
        }
    }

    protected void processMessage(String topic, MqttMessage msg) {
        Map<String, Object> payload;
        String rawData = new String(msg.getPayload());

        try {
            payload = objectMapper.readValue(rawData, Map.class);
        } catch (Exception e) {
            payload = new HashMap<>();
            payload.put("rawPayload", rawData);
        }

        payload.put("topic", topic);
        payload.put("mqttTimestamp", System.currentTimeMillis());

        send("out", new Message(payload));
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
    protected byte[] readData() throws IOException {
        return null;
    }
}

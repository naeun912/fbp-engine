package com.fbp.engine.runner.scenario;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.CollectorNode;
import com.fbp.engine.node.protocolImpl.MqttPublisherNode;
import com.fbp.engine.node.protocolImpl.MqttSubscriberNode;
import com.fbp.engine.node.rule.CompositeRuleNode;
import com.fbp.engine.node.rule.Operator;

import java.util.Map;

public class Scenario1_MqttToMqtt {
    public static void main(String[] args) throws InterruptedException {
        Flow flow = new Flow("flow");
        FlowEngine engine = new FlowEngine();
        CompositeRuleNode ruleNode = new CompositeRuleNode("rule", Operator.AND);
        ruleNode.addCondition("value", ">", 30.0);
        MqttSubscriberNode subscriberNode = new MqttSubscriberNode("sub", Map.of(
                "brokerUrl", "tcp://127.0.0.1:1884",
                "topic", "sensor/temp",
                "clientId", "sub-client-001" // 💡 고유한 ID 부여
        ));

        MqttPublisherNode pubNode = new MqttPublisherNode("pub", Map.of(
                "brokerUrl", "tcp://127.0.0.1:1884",
                "topic", "alert/temp",
                "clientId", "pub-client-001" // 💡 고유한 ID 부여
        ));

        CollectorNode mismatchCol = new CollectorNode("mismatch-col");

        flow.addNode(subscriberNode).addNode(ruleNode).addNode(pubNode).addNode(mismatchCol);

        flow.connect("sub", "out", "rule", "in")
                .connect("rule", "match", "pub", "in")
                .connect("rule", "mismatch", "mismatch-col", "in");

        engine.register(flow);
        engine.startFlow("flow");

        System.out.println("🚀 시나리오 1 실행 중... (MQTT 브로커 1884 연결됨)");


        Thread.sleep(3000);
        ruleNode.onProcess(new Message(Map.of("value", 35.0)));
        Thread.sleep(5000);

        engine.stopFlow("flow");
    }
}

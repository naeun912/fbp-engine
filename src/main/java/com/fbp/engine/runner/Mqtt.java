package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.abstractImpl.FilterNode;
import com.fbp.engine.node.protocolImpl.MqttPublisherNode;
import com.fbp.engine.node.protocolImpl.MqttSubscriberNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class Mqtt {

    public static void main(String[] args) {
        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        Map<String, Object> subConfig = new HashMap<>();
        subConfig.put("brokerUrl", "tcp://localhost:1883");
        subConfig.put("clientId", "naeun-sub-temp");
        subConfig.put("topic", "sensor/temp");
        MqttSubscriberNode subNode = new MqttSubscriberNode("subTemp", subConfig);

        FilterNode filterNode = new FilterNode("tempFilter", "temperature", 30.0);

        Map<String, Object> pubConfig = new HashMap<>();
        pubConfig.put("brokerUrl", "tcp://localhost:1883");
        pubConfig.put("clientId", "naeun-pub-alert");
        pubConfig.put("topic", "alert/temp");
        MqttPublisherNode pubNode = new MqttPublisherNode("pubAlert", pubConfig);

        flow.addNode(subNode).addNode(filterNode).addNode(pubNode)
                .connect("subTemp", "out", "tempFilter", "in")
                .connect("tempFilter", "out", "pubAlert", "in");

        engine.register(flow);
        engine.startFlow("flow");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        engine.stopFlow("flow");
    }
}

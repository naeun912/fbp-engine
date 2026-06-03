package com.fbp.engine.runner.modbus;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.modbus.ModbusReaderNode;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.node.abstractImpl.PrintNode;
import com.fbp.engine.node.abstractImpl.TimerNode;
import com.fbp.engine.node.protocolImpl.MqttPublisherNode;
import com.fbp.engine.node.rule.CompositeRuleNode;
import com.fbp.engine.node.rule.Operator;

import java.util.HashMap;
import java.util.Map;

public class EndToEndFlow {

    public static void main(String[] args) {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5025, 100);
        try {
            simulator.start();
            System.out.println("✅ Modbus 시뮬레이터 서버 시작됨 (Port: 5025)");

            Thread.sleep(1000);
            simulator.setRegister(0, 20); // 온도 20도로 낮춤 (조건 불만족)
            simulator.setRegister(1, 50); // 습도 50%로 낮춤 (조건 불만족)

        } catch (Exception e) {
            System.err.println(STR."❌ 서버 시작 실패: \{e.getMessage()}");
        }


        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");


        // 1. ModbusReaderNode 설정 (기존과 동일)
        Map<String, Object> readerConfig = new HashMap<>();
        readerConfig.put("host", "127.0.0.1");
        readerConfig.put("port", 5025);
        readerConfig.put("slaveId", 1);
        readerConfig.put("startAddress", 0);
        readerConfig.put("count", 2);
        Map<String, Object> readerMapping = new HashMap<>();
        readerMapping.put("temperature", 0);
        readerMapping.put("humidity", 1);
        readerConfig.put("registerMapping", readerMapping);

        Map<String, Object> alertMqttConfig = new HashMap<>();
        alertMqttConfig.put("brokerUrl", "tcp://broker.hivemq.com:1883");
        alertMqttConfig.put("clientId", STR."fbp-alert-node-\{System.currentTimeMillis()}");
        alertMqttConfig.put("topic", "factory/alerts"); // 알람 토픽
        alertMqttConfig.put("qos", 1);

        Map<String, Object> normalMqttConfig = new HashMap<>();
        normalMqttConfig.put("brokerUrl", "tcp://broker.hivemq.com:1883");
        normalMqttConfig.put("clientId", STR."fbp-normal-node-\{System.currentTimeMillis()}");
        normalMqttConfig.put("topic", "factory/status"); // 정상 상태 토픽
        normalMqttConfig.put("qos", 0);

        ModbusReaderNode reader = new ModbusReaderNode("reader-01", readerConfig);
        CompositeRuleNode rule = new CompositeRuleNode("critical-logic", Operator.AND);
        MqttPublisherNode alertPublisher = new MqttPublisherNode("mqtt-alert-pub", alertMqttConfig);
        MqttPublisherNode normalPublisher = new MqttPublisherNode("mqtt-normal-pub", normalMqttConfig);

        // [추가] 검증 ㅌ통과를 위한 트리거 노드와 로그 노드
        TimerNode timerNode = new TimerNode("timer", 1000);
        PrintNode printer = new PrintNode("printer");

        rule.addCondition("temperature", ">", 30.0);
        rule.addCondition("humidity", ">", 80.0);

        flow.addNode(timerNode).addNode(reader).addNode(rule)
                .addNode(alertPublisher).addNode(normalPublisher).addNode(printer);


        flow.connect("timer", "out", "reader-01", "trigger")
                .connect("reader-01", "out", "critical-logic", "in")
                .connect("reader-01", "error", "printer", "in")
                .connect("critical-logic", "match", "mqtt-alert-pub", "in")
                .connect("critical-logic", "mismatch", "mqtt-normal-pub", "in");

        engine.register(flow);
        engine.startFlow("flow");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException _) {
        }
        engine.stopFlow("flow");
    }
}
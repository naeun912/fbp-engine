package com.fbp.engine.runner.scenario;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.modbus.ModbusWriterNode;
import com.fbp.engine.node.abstractImpl.CollectorNode;
import com.fbp.engine.node.abstractImpl.PrintNode;
import com.fbp.engine.node.protocolImpl.MqttSubscriberNode;
import com.fbp.engine.node.rule.CompositeRuleNode;
import com.fbp.engine.node.rule.Operator;

import java.util.Map;

public class Scenario3_CrossProtocol {

    public static void main(String[] args) throws InterruptedException {

        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 100);
        try {
            simulator.start();
            System.out.println("✅ Modbus 시뮬레이터 서버 시작됨 (Port: 5025)");

            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println(STR."❌ 서버 시작 실패: \{e.getMessage()}");
        }

        Flow flow = new Flow("flow");
        FlowEngine engine = new FlowEngine();
        MqttSubscriberNode mqttSub = new MqttSubscriberNode("mqtt-sub", Map.of(
                "brokerUrl", "tcp://127.0.0.1:1884",
                "topic", "cmd/device",
                "clientId", "sub-client-001" // 💡 고유한 ID 부여
        ));
        CompositeRuleNode rule = new CompositeRuleNode("rule", Operator.AND);
        rule.addCondition("power", "==", 1.0);
        ModbusWriterNode modbusWrite = new ModbusWriterNode("modbus-write", Map.of(
                "host", "127.0.0.1", "port", 5020, "slaveId", 1, "registerAddress", 20, "valueField", "power"
        ));

        // validate 통과용
        PrintNode printer = new PrintNode("log");
        CollectorNode resultCol = new CollectorNode("result");

        flow.addNode(mqttSub).addNode(rule).addNode(modbusWrite).addNode(printer).addNode(resultCol);
        flow.connect("mqtt-sub", "out", "rule", "in")
                .connect("rule", "match", "modbus-write", "in")
                .connect("rule", "mismatch", "log", "in")
                .connect("modbus-write", "result", "result", "in");

        engine.register(flow);
        engine.startFlow("flow");

        System.out.println("🚀 시나리오 3 실행 중... (MQTT 1884 <-> Modbus 5020)");


        Thread.sleep(3000);
        int count = 100;
        long totalTime = 0;

        for (int i = 0; i < count; i++) {
            long start = System.nanoTime();
            mqttSub.onProcess(new Message(Map.of("power", 1.0)));
            totalTime += (System.nanoTime() - start);
        }

        double avgDuration = (totalTime / (double) count) / 1_000_000.0;
        System.out.println("⏱ 평균 지연 시간 (100회 평균): " + avgDuration + " ms");
//        engine.stopFlow("flow");

    }
}

package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.modbus.ModbusWriterNode;
import com.fbp.engine.node.abstractImpl.CollectorNode;
import com.fbp.engine.node.protocolImpl.MqttPublisherNode;
import com.fbp.engine.node.protocolImpl.MqttSubscriberNode;
import com.fbp.engine.node.rule.CompositeRuleNode;
import com.fbp.engine.node.rule.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
public class MqttModbusIntegrationTest {
    final int SIMULATOR_PORT = 5020;
    final int TARGET_REGISTER = 40001;
    FlowEngine engine;
    Flow flow;
    MqttPublisherNode publisherNode;
    MqttSubscriberNode subscriberNode;
    CollectorNode matchNode;
    CollectorNode mismatchNode;
    ModbusWriterNode writerNode;
    ModbusTcpSimulator simulator;
    CompositeRuleNode ruleNode;

    @BeforeEach
    void setUp() {

        simulator = new ModbusTcpSimulator(SIMULATOR_PORT, 10000);
        simulator.start();
        System.out.println(STR."✅ Modbus 시뮬레이터 서버 시작됨 (Port: \{SIMULATOR_PORT})");
        engine = new FlowEngine();
        flow = new Flow("flow");
        engine.register(flow);
    }

    @Test
    void fullIntegrationPipelineTest() {
        subscriberNode = new MqttSubscriberNode("in", Map.of("brokerUrl", "tcp://localhost:1883", "topic", "sensor/humidity"));
        ruleNode = new CompositeRuleNode("humidity-rule", Operator.AND);
        ruleNode.addCondition("humidity", ">", 70.0);

        writerNode = new ModbusWriterNode("writer", Map.of(
                "host", "127.0.0.1", "port", SIMULATOR_PORT, "slaveId", 1,
                "registerAddress", TARGET_REGISTER, "valueField", "humidity"
        ));

        publisherNode = new MqttPublisherNode("alert", Map.of("brokerUrl", "tcp://localhost:1883", "topic", "factory/alert"));

        matchNode = new CollectorNode("alert-verify-col");
        mismatchNode = new CollectorNode("mismatch-log-col");
        CollectorNode modbusResultCol = new CollectorNode("modbus-result-col");

        flow.addNode(subscriberNode).addNode(ruleNode).addNode(writerNode).addNode(publisherNode).addNode(matchNode).addNode(mismatchNode).addNode(modbusResultCol);

        flow.connect("in", "out", "humidity-rule", "in")
                .connect("humidity-rule", "match", "writer", "in")
                .connect("humidity-rule", "match", "alert", "in")
                .connect("humidity-rule", "mismatch", "alert-verify-col", "in")
                .connect("humidity-rule", "mismatch", "mismatch-log-col", "in")
                .connect("writer", "result", "modbus-result-col", "in");

        engine.startFlow("flow");

        ruleNode.onProcess(new Message(Map.of("humidity", 85.0)));

        assertEquals(85, simulator.getRegister(40001));
        assertEquals(1, matchNode.getCollected().size());
        System.out.println("동작 성공");
    }

    @AfterEach
    void tearDown() {
        simulator.stop();
        engine.shutdown();
    }

}


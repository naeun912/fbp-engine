package com.fbp.engine.runner.scenario;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.modbus.ModbusReaderNode;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.modbus.ModbusWriterNode;
import com.fbp.engine.node.abstractImpl.CollectorNode;
import com.fbp.engine.node.abstractImpl.PrintNode;
import com.fbp.engine.node.abstractImpl.TimerNode;
import com.fbp.engine.node.rule.CompositeRuleNode;
import com.fbp.engine.node.rule.Operator;

import java.util.Map;

public class Scenario2_ModbusLoop {
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

        TimerNode timer = new TimerNode("timer", 1000);
        ModbusReaderNode reader = new ModbusReaderNode("reader", Map.of(
                "host", "127.0.0.1", "port", 5020, "slaveId", 1, "startAddress", 0, "count", 1,
                "registerMapping", Map.of("humidity", 0)
        ));
        CompositeRuleNode rule = new CompositeRuleNode("rule", Operator.AND);
        rule.addCondition("humidity", ">", 70.0);
        ModbusWriterNode writer = new ModbusWriterNode("writer", Map.of(
                "host", "127.0.0.1", "port", 5020, "slaveId", 1, "registerAddress", 10, "valueField", "humidity"
        ));

        // validate 통과용
        PrintNode printer = new PrintNode("log");
        CollectorNode resultCol = new CollectorNode("result");

        flow.addNode(timer).addNode(reader).addNode(rule).addNode(writer).addNode(printer).addNode(resultCol);
        flow.connect("timer", "out", "reader", "trigger")
                .connect("reader", "out", "rule", "in")
                .connect("reader", "error", "log", "in")
                .connect("rule", "match", "writer", "in")
                .connect("rule", "mismatch", "log", "in")
                .connect("writer", "result", "result", "in");

        engine.register(flow);
        engine.startFlow("flow");

        System.out.println("🚀 시나리오 2 실행 중... (Modbus 5020 연결됨)");

        simulator.setRegister(0, 85);

        Thread.sleep(15000);

        engine.stopFlow("flow");

    }
}

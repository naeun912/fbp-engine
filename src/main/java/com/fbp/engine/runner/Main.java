package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.modbus.ModbusReaderNode;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.node.abstractImpl.PrintNode;
import com.fbp.engine.node.abstractImpl.TimerNode;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 5020;

        // 1. 시뮬레이터(보관함) 먼저 기동하고, 테스트할 초기값 채워두기
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(port, 10);
        simulator.setRegister(0, 250);
        simulator.setRegister(1, 600);
        simulator.setRegister(2, 1);
        simulator.start(); //

        // 2. 3-8 전용 플로우 박스 생성
        Flow flow = new Flow("stage-3-8-flow");

        // 3. ModbusReaderNode용 설정지(Config) 작성
        Map<String, Object> readerConfig = Map.of(
                "host", "localhost",
                "port", port,
                "slaveId", 1,
                "startAddress", 0,
                "count", 3
        );

        // 4. 레고 블록(노드) 생성하기
        TimerNode timerNode = new TimerNode("timer", 1000);
        ModbusReaderNode readerNode = new ModbusReaderNode("reader", readerConfig);
        PrintNode printNode = new PrintNode("printer");

        // 5. 플로우에 블록들 등록하기
        flow.addNode(timerNode)
                .addNode(readerNode)
                .addNode(printNode);

        flow.connect("timer", "out", "reader", "trigger");
        flow.connect("reader", "out", "printer", "in");

        // 7. 네트워크 통신선 활성화 및 초기화
        readerNode.connect();

        System.out.println("Flow 검증 오류 리스트: " + flow.validate());
        flow.initialize();

        System.out.println("과제 3-8 플로우가 가동되었습니다. 1초마다 화면에 찍힙니다... (5초간 실행)");
        Thread.sleep(5000);

        flow.shutdown();
        readerNode.disconnect();
        simulator.stop();
        System.out.println("테스트가 안전하게 종료되었습니다.");
    }
}
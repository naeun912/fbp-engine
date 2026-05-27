package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.modbus.ModbusReaderNode;
import com.fbp.engine.modbus.ModbusTcpSimulator;
import com.fbp.engine.modbus.ModbusWriterNode;
import com.fbp.engine.node.abstractImpl.ThresholdFilterNode;
import com.fbp.engine.node.abstractImpl.TimerNode;

import java.util.Map;

public class ModbusFlowTest {
    public static void main(String[] args) throws Exception {
        int port = 5020;

        // 1. 시뮬레이터 가동 및 초기값 세팅
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(port, 10);
        simulator.setRegister(0, 35);
        simulator.setRegister(2, 0);
        simulator.start(); //

        
        Flow flow = new Flow("stage-3-9-control-flow");

        // 3. 노드 설정지(Config) 작성
        // 리더 노드가 읽은 0번 방 값을 "temperature"라는 이름으로 매핑해서 메시지에 담도록 설정
        Map<String, Object> readerConfig = Map.of(
                "host", "localhost",
                "port", port,
                "slaveId", 1,
                "startAddress", 0,
                "count", 1,
                "registerMapping", Map.of("temperature", 0) // {"temperature": 0} 매핑 추가
        );

        // 라이터 노드는 메시지 내부에서 "alertCode"라는 키를 찾아 그 값을 2번 방에 적을 것입니다.
        Map<String, Object> writerConfig = Map.of(
                "host", "localhost",
                "port", port,
                "slaveId", 1,
                "registerAddress", 2,      // 2번 레지스터에 기록
                "valueField", "alertCode", // 메시지에서 "alertCode"를 꺼내서 씀
                "scale", 1.0
        );

        // 4. 노드 생성 (보내주신 ThresholdFilterNode 스펙 적용)
        TimerNode timerNode = new TimerNode("timer", 1000);
        ModbusReaderNode readerNode = new ModbusReaderNode("reader", readerConfig);

        // 💡 중요: "temperature" 필드를 검사하고, 임계값은 30.0으로 세팅
        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30.0);
        ModbusWriterNode writerNode = new ModbusWriterNode("writer", writerConfig);

        // 플로우에 노드 등록
        flow.addNode(timerNode)
                .addNode(readerNode)
                .addNode(filterNode)
                .addNode(writerNode);

        // 5. 🚨 진짜 포트 이름 매칭해서 connect로 연결선 조립하기!
        flow.connect("timer", "out", "reader", "trigger");
        flow.connect("reader", "out", "filter", "in");

        // 💡 중요: 필터 노드의 "alert" 포트와 라이터 노드의 "in" 포트를 연결!
        flow.connect("filter", "alert", "writer", "in");

        // 6. 소켓 연결 및 엔진 가동
        readerNode.connect();
        writerNode.connect();

        flow.initialize();
        flow.setState(Flow.State.RUNNING);

        System.out.println("과제 3-9 제어 플로우 가동 중... (온도가 35도이므로 잠시 후 자동으로 알림이 켜져야 합니다)");

        // 💡 잠깐! 라이터 노드가 "alertCode"를 꺼내 쓸 수 있도록
        // 타이머 노드가 처음 던지는 메시지나 시스템 베이스에 임시로 강제 주입해 보거나,
        // 혹은 테스트를 위해 쓰기 노드가 작동하도록 3초 대기합니다.
        Thread.sleep(3000);

        // 7. 결과 검증: 35도였으니 필터의 alert를 타고 라이터가 실행되어 2번 방이 1이 되었는지 확인
        System.out.println("--- 검증 결과 ---");
        System.out.println("시뮬레이터 2번 레지스터(제어 알림값): " + simulator.getRegister(2));
        // 만약 0이 나온다면, FilterNode가 보내는 alertMessage에 "alertCode": 1.0 처럼
        // 라이터가 꺼낼 키값을 담아 흘려보내주는 브릿지 노드가 중간에 필요하거나,
        // FilterNode 내부에서 `message.withEntry("alertCode", 1.0)`을 해주어야 완벽히 연동됩니다!

        // 8. 자원 정리
        flow.shutdown();
        readerNode.disconnect();
        writerNode.disconnect();
        simulator.stop(); //
    }
}
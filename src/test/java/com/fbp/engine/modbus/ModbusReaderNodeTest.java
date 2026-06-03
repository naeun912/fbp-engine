package com.fbp.engine.modbus;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModbusReaderNodeTest {
    private static final int PORT = 5025;
    private static ModbusTcpSimulator simulator;
    private ModbusReaderNode node;
    private Connection outConn;
    private Connection errorConn;

    @BeforeAll
    static void startSimulator() {
        simulator = new ModbusTcpSimulator(PORT, 100);
        simulator.start();
    }

    @AfterAll
    static void stopSimulator() {
        simulator.stop();
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", PORT);
        config.put("slaveId", 1);
        config.put("startAddress", 0);
        config.put("count", 5);

        Map<String, Object> mapping = new HashMap<>();
        mapping.put("temperature", 0);
        mapping.put("humidity", 1);
        config.put("registerMapping", mapping);

        node = new ModbusReaderNode("node-8-test", config);

        outConn = new Connection("out-conn");
        errorConn = new Connection("err-conn");
        node.getOutputPort("out").connect(outConn);
        node.getOutputPort("error").connect(errorConn);
    }

    // ================= [ 단위 테스트 ] =================

    @Test
    @Order(1)
    @DisplayName("#1: 포트 구성 - trigger, out, error 존재 확인")
    void test1_PortConfiguration() {
        assertNotNull(node.getInputPort("trigger"));
        assertNotNull(node.getOutputPort("out"));
        assertNotNull(node.getOutputPort("error"));
    }

    @Test
    @Order(2)
    @DisplayName("#2: 초기 상태 - isConnected()는 false여야 함")
    void test2_InitialState() {
        assertFalse(node.isConnected());
    }

    @Test
    @Order(3)
    @DisplayName("#3: config 확인 - host, slaveId 등 설정값 일치")
    void test3_ConfigMatching() {
        assertEquals("localhost", node.getHost());
        assertEquals(1, node.getSlaveId());
    }

    // ================= [ 통합 테스트 ] =================

    @Test
    @Order(4)
    @DisplayName("#4: 연결 성공 - connect() 후 isConnected()는 true")
    void test4_ConnectionSuccess() throws Exception {
        node.initialize();
        assertTrue(node.isConnected());
    }

    @Test
    @Order(5)
    @DisplayName("#5: 레지스터 읽기 - trigger 메시지 후 데이터 수신")
    void test5_RegisterRead() throws Exception {
        node.initialize();
        simulator.setRegister(0, 100);

        node.onProcess(new Message(new HashMap<>())); // Trigger 전송

        Message msg = outConn.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg);
    }

    @Test
    @Order(6)
    @DisplayName("#6: 매핑 적용 - raw 데이터를 temp/humi로 변환")
    void test6_RegisterMapping() throws Exception {
        node.connect();
        simulator.setRegister(0, 255); // temperature 25.5도 가정

        node.onProcess(new Message(new HashMap<>()));

        Message msg = outConn.poll(1, TimeUnit.SECONDS);
        // 나은님의 onProcess 로직에 따라 매핑된 키값이 존재해야 함
        assertNotNull(msg.get("temperature"));
        assertEquals(255, (Integer) msg.get("temperature"));
    }

    @Test
    @Order(7)
    @DisplayName("#7: 읽기 실패 - 존재하지 않는 주소 시 error 포트 전송")
    void test7_ReadErrorHandling() throws Exception {
        // 강제로 잘못된 주소 범위를 가진 노드 생성 시뮬레이션
        Map<String, Object> badConfig = new HashMap<>();
        badConfig.put("host", "localhost");
        badConfig.put("port", PORT);
        badConfig.put("slaveId", 1);
        badConfig.put("startAddress", 999); // 잘못된 주소
        badConfig.put("count", 1);

        ModbusReaderNode badNode = new ModbusReaderNode("bad-node", badConfig);
        badNode.getOutputPort("error").connect(errorConn);
        badNode.initialize();

        badNode.onProcess(new Message(new HashMap<>()));

        Message err = errorConn.poll(1, TimeUnit.SECONDS);
        assertNotNull(err);
        assertTrue(err.hasKey("error"));
    }

    @Test
    @Order(8)
    @DisplayName("#8: shutdown 후 연결 해제 - isConnected()는 false")
    void test8_ShutdownRelease() throws Exception {
        node.initialize();
        node.shutdown();
        assertFalse(node.isConnected());
    }
}
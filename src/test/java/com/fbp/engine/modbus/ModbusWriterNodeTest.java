package com.fbp.engine.modbus;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModbusWriterNodeTest {
    private static final int PORT = 5026;
    private static ModbusTcpSimulator simulator;
    private ModbusWriterNode node;

    @BeforeAll
    static void startSimulator() throws InterruptedException {
        simulator = new ModbusTcpSimulator(PORT, 100);
        simulator.start();
        Thread.sleep(500); // 시뮬레이터 준비 시간
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
        config.put("registerAddress", 50); // 테스트용 번지
        config.put("scale", 1.0); // 기본 스케일
        config.put("valueField", "value"); // <--- 이 부분이 빠져있었습니다!

        node = new ModbusWriterNode("write-node", config);

        // 2. [가장 중요] initialize() 하기 전에 Connection을 먼저 꽂아준다!
        Connection conn = new Connection("test-conn");

        ((DefaultInputPort) node.getInputPort("in")).setConnection(conn);
    }

    // ================= [ 단위 테스트 ] =================

    @Test
    @Order(1)
    @DisplayName("단위 #1: 포트 구성 확인 - in 포트 존재 여부")
    void test1_PortIntegrity() {
        assertNotNull(node.getInputPort("in"));
    }

    @Test
    @Order(2)
    @DisplayName("단위 #2: 초기 상태 확인 - 생성 직후 disconnected")
    void test2_InitialState() {
        assertFalse(node.isConnected());
    }

    @Test
    @Order(3)
    @DisplayName("단위 #3: Config 확인 - 설정값 일치 여부")
    void test3_ConfigCheck() {
        assertEquals(50, node.getConfig("registerAddress"));
    }

    // ================= [ 통합 테스트 ] =================

    @Test
    @Order(4)
    @DisplayName("통합 #4: 연결 성공 - initialize() 후 상태 변화")
    void test4_Initialization() {
        node.initialize();
        assertTrue(node.isConnected());
    }

    @Test
    @Order(5)
    @DisplayName("통합 #5: 레지스터 쓰기 - process() 후 시뮬레이터 값 확인")
    void test5_WriteExecution() {
        node.initialize();
        int writeValue = 777;

        // 페이로드에 값을 담아 전송 (ModbusWriteNode의 로직에 따라 키값 조정 필요)
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", writeValue);

        node.onProcess(new Message(payload));

        // 시뮬레이터의 50번지 값이 바뀌었는지 확인
        assertEquals(writeValue, simulator.getRegister(50));
    }

    @Test
    @Order(6)
    @DisplayName("통합 #6: 스케일 변환 - 25.5 -> 255 변환 확인")
    void test6_ScaleConversion() {
        // 스케일이 10.0인 새로운 노드 설정
        Map<String, Object> scaleConfig = new HashMap<>();
        scaleConfig.put("host", "localhost");
        scaleConfig.put("port", PORT);
        scaleConfig.put("slaveId", 1);
        scaleConfig.put("registerAddress", 60);
        scaleConfig.put("scale", 10.0);
        scaleConfig.put("valueField", "value");

        ModbusWriterNode scaleNode = new ModbusWriterNode("scale-node", scaleConfig);
        scaleNode.initialize();

        Map<String, Object> payload = new HashMap<>();
        payload.put("value", 25.5); // 25.5 입력

        scaleNode.onProcess(new Message(payload));

        assertEquals(255, simulator.getRegister(60));
    }

    @Test
    @Order(7)
    @DisplayName("통합 #7: shutdown 후 연결 해제")
    void test7_ShutdownLifecycle() {
        node.initialize();
        node.shutdown();
        assertFalse(node.isConnected());
    }
}
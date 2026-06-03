package com.fbp.engine.modbus;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModbusTcpClientSimulatorTest {
    private static final int PORT = 5021; // 테스트용 포트
    private static ModbusTcpSimulator simulator;
    private ModbusTcpClient client;

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
    void connectClient() throws IOException {
        client = new ModbusTcpClient("localhost", PORT);
        client.connect();
    }

    @AfterEach
    void closeClient() {
        client.disconnect();
    }

    // ------------------------------------------------------------
    // #6. 연결해제 (Disconnect)
    // ------------------------------------------------------------
    @Test
    @Order(6)
    @DisplayName("테스트 #6: 연결 및 해제 상태 검증")
    void test6_ConnectionAndDisconnect() throws IOException {
        assertTrue(client.isConnected());

        client.disconnect();
        assertFalse(client.isConnected());
    }

    // ------------------------------------------------------------
    // #7. Holding Register 읽기 (FC 03)
    // ------------------------------------------------------------
    @Test
    @Order(7)
    @DisplayName("테스트 #7: 단일 레지스터 읽기 검증")
    void test7_ReadSingleRegister() throws Exception {
        int targetAddr = 10;
        int expectedValue = 1234;

        simulator.setRegister(targetAddr, expectedValue);

        int[] result = client.readHoldingRegisters(1, targetAddr, 1);
        assertEquals(expectedValue, result[0]);
    }

    // ------------------------------------------------------------
    // #8. 다수 레지스터 읽기 (FC 03)
    // ------------------------------------------------------------
    @Test
    @Order(8)
    @DisplayName("테스트 #8: 5개 레지스터 동시 읽기 검증")
    void test8_ReadMultipleRegisters() throws Exception {
        int startAddr = 20;
        int[] values = {10, 20, 30, 40, 50};

        for (int i = 0; i < values.length; i++) {
            simulator.setRegister(startAddr + i, values[i]);
        }

        int[] result = client.readHoldingRegisters(1, startAddr, 5);

        assertAll(
                () -> assertEquals(5, result.length),
                () -> assertEquals(10, result[0]),
                () -> assertEquals(50, result[4])
        );
    }

    // ------------------------------------------------------------
    // #9. Single Register 쓰기 (FC 06)
    // ------------------------------------------------------------
    @Test
    @Order(9)
    @DisplayName("테스트 #9: 레지스터 쓰기 후 시뮬레이터 값 확인")
    void test9_WriteSingleRegister() throws Exception {
        int addr = 30;
        int valueToWrite = 777;

        client.writeSingleRegister(1, addr, valueToWrite);

        assertEquals(valueToWrite, simulator.getRegister(addr));
    }

    // ------------------------------------------------------------
    // #10. 쓰기 후 읽기 (Sequence)
    // ------------------------------------------------------------
    @Test
    @Order(10)
    @DisplayName("테스트 #10: 쓰기 작업 직후 읽기 작업 수행")
    void test10_WriteAndReadSequence() throws Exception {
        int addr = 40;
        int value = 888;

        client.writeSingleRegister(1, addr, value);
        int[] result = client.readHoldingRegisters(1, addr, 1);

        assertEquals(value, result[0]);
    }

    // ------------------------------------------------------------
    // #11. 예외 응답 처리 (Illegal Address)
    // ------------------------------------------------------------
    @Test
    @Order(11)
    @DisplayName("테스트 #11: 존재하지 않는 주소 요청 시 예외 발생")
    void test11_IllegalAddressError() {
        // 시뮬레이터는 100개(0~99)만 가짐. 999번지 요청 시 0x02 에러 응답 기대
        assertThrows(RuntimeException.class, () -> {
            client.readHoldingRegisters(1, 999, 1);
        });
    }

    // ------------------------------------------------------------
    // #12. 소켓 타임아웃 (Resilience)
    // ------------------------------------------------------------
    @Test
    @Order(12)
    @DisplayName("테스트 #12: 서버 중단 시 네트워크 예외 처리")
    void test12_SocketResilience() {
        simulator.stop();

        assertThrows(SocketTimeoutException.class, () -> {
            client.readHoldingRegisters(1, 0, 1);
        });

        simulator.start();
    }
}
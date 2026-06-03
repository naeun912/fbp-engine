package com.fbp.engine.modbus;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModbusTcpSimulatorTest {
    private static final int PORT = 5022;
    private ModbusTcpSimulator simulator;
    private ModbusTcpClient client;

    @BeforeEach
    void setUp() {
        simulator = new ModbusTcpSimulator(PORT, 100); // 100개 레지스터
    }

    @AfterEach
    void tearDown() {
        if (client != null) client.disconnect();
        simulator.stop();
    }

    @Test
    @Order(1)
    @DisplayName("#1: 시작/종료")
    void test1_StartStop() throws IOException {
        simulator.start();

        // 포트가 열렸는지 실제 소켓으로 확인
        try (Socket socket = new Socket("localhost", PORT)) {
            assertTrue(socket.isConnected());
        }

        simulator.stop();

        // 종료 후 연결 시도 시 에러 발생 확인
        assertThrows(IOException.class, () -> new Socket("localhost", PORT));
    }

    @Test
    @Order(2)
    @DisplayName("#2: 레지스터 초기값")
    void test2_RegisterInitialization() {
        simulator.setRegister(0, 100);
        simulator.setRegister(99, 9999);

        assertEquals(100, simulator.getRegister(0));
        assertEquals(9999, simulator.getRegister(99));
    }

    @Test
    @Order(3)
    @DisplayName("#3: FC 03 응답")
    void test3_FC03Response() throws Exception {
        simulator.start();
        simulator.setRegister(10, 555);

        client = new ModbusTcpClient("localhost", PORT);
        client.connect();

        int[] result = client.readHoldingRegisters(1, 10, 1);
        assertEquals(555, result[0]);
    }

    @Test
    @Order(4)
    @DisplayName("#4: FC 06 응답")
    void test4_FC06Response() throws Exception {
        simulator.start();
        client = new ModbusTcpClient("localhost", PORT);
        client.connect();

        client.writeSingleRegister(1, 20, 777);

        assertEquals(777, simulator.getRegister(20));
    }

    @Test
    @Order(5)
    @DisplayName("#5: 잘못된 주소 에러")
    void test5_InvalidAddressError() throws Exception {
        simulator.start();
        client = new ModbusTcpClient("localhost", PORT);
        client.connect();

        assertThrows(RuntimeException.class, () -> client.readHoldingRegisters(1, 100, 1));
    }

    @Test
    @Order(6)
    @DisplayName("#6: 다중 클라이언트 - 2개 클라이언트 독립적 처리")
    void test6_MultipleClients() throws Exception {
        simulator.start();
        simulator.setRegister(1, 111);
        simulator.setRegister(2, 222);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 클라이언트 1 테스트
        executor.execute(() -> {
            ModbusTcpClient c1 = new ModbusTcpClient("localhost", PORT);
            try {
                c1.connect();
                assertEquals(111, c1.readHoldingRegisters(1, 1, 1)[0]);
            } catch (Exception e) {
                e.printStackTrace();
                fail("클라이언트 1 통신 실패");
            } finally {
                c1.disconnect();
            }
        });

        // 클라이언트 2 테스트
        executor.execute(() -> {
            ModbusTcpClient c2 = new ModbusTcpClient("localhost", PORT);
            try {
                c2.connect();
                assertEquals(222, c2.readHoldingRegisters(1, 2, 1)[0]);
            } catch (Exception e) {
                e.printStackTrace();
                fail("클라이언트 2 통신 실패");
            } finally {
                c2.disconnect();
            }
        });

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

}
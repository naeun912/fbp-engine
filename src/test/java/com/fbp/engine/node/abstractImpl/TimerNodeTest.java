package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerNodeTest {
    TimerNode timerNode;
    Connection connection;

    @BeforeEach
    void setUp() {
        timerNode = new TimerNode("timer - 1", 1000);
        connection = new Connection("connection - 1");
    }

    @Test
    @DisplayName("initialize 후 메세지 생성")
    void initializeTest() {
        timerNode.getOutputPort("out").connect(connection);

        timerNode.initialize();

        try {
            Thread.sleep(timerNode.getIntervalMs() + 100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Message message = connection.poll();

        assertNotNull(message);
        assertTrue(message.payload().containsKey("tick"));

        timerNode.shutdown();
    }

    @Test
    @DisplayName("tick 증가")
    void tickCountTest() {

        timerNode.getOutputPort("out").connect(connection);

        timerNode.initialize();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Message message = connection.poll();
        Message message1 = connection.poll();
        Message message2 = connection.poll();

        assertNotNull(message);
        assertNotNull(message1);
        assertNotNull(message2);

        int tick0 = (int) message.get("tick");
        int tick1 = (int) message1.get("tick");
        int tick2 = (int) message2.get("tick");

        assertEquals(tick0 + 1, tick1);
        assertEquals(tick1 + 1, tick2);

        timerNode.shutdown();
    }

    @Test
    @DisplayName("shutdown 후 정지")
    void shutdownStopTest() {
        timerNode.getOutputPort("out").connect(connection);

        timerNode.initialize();


        try {
            Thread.sleep(timerNode.getIntervalMs() * 3 + 100);
            int countBeforeShutdown = 0;
            while (!connection.getBuffer().isEmpty()) {
                connection.poll();
                countBeforeShutdown++;
            }
            assertTrue(countBeforeShutdown > 0);

            timerNode.shutdown();

            Thread.sleep(1000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int countAfterShutdown = 0;
        while (!connection.getBuffer().isEmpty()) {
            connection.poll();
            countAfterShutdown++;
        }

        assertEquals(0, countAfterShutdown, "shutdown 후에는 새로운 메시지가 쌓이면 안 됩니다.");
    }

    @Test
    @DisplayName("주기 확인")
    void intervalMsTest() {
        timerNode = new TimerNode("timer - 2", 500);

        timerNode.getOutputPort("out").connect(connection);

        timerNode.initialize();

        try {
            Thread.sleep(2100);
            timerNode.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int count = 0;
        while (!connection.getBuffer().isEmpty()) {
            connection.poll();
            count++;
        }

        System.out.println("만들어진 메시지 개수: " + count);
        assertTrue(count >= 3 && count <= 5);

        Thread.currentThread().interrupt();
    }

}
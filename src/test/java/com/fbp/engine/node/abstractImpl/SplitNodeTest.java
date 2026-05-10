package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SplitNodeTest {
    SplitNode splitNode;
    Connection connection1;
    Connection connection2;

    @BeforeEach
    void setUp() {
        connection1 = new Connection("match");
        connection2 = new Connection("mismatch");
        splitNode = new SplitNode("split - 1", "temperature", 30.0);
        splitNode.getOutputPort("match").connect(connection1);
        splitNode.getOutputPort("mismatch").connect(connection2);
    }

    @Test
    @DisplayName("조건 만족 → match 포트")
    void matchPortTest() {
        Message message = new Message(Map.of("temperature", 35.5));
        splitNode.onProcess(message);
        assertNotNull(connection1.poll());
        try {
            assertNull(connection2.poll(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("조건 미달 → mismatch 포트")
    void misMatchPortTest() {
        Message message = new Message(Map.of("temperature", 25.5));
        splitNode.onProcess(message);
        assertNotNull(connection2.poll());
        try {
            assertNull(connection1.poll(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("양쪽 동시 확인")
    void allPortTest() {
        Message message = new Message(Map.of("temperature", 35.5));
        Message message1 = new Message(Map.of("temperature", 25.5));

        splitNode.onProcess(message);
        splitNode.onProcess(message1);

        assertNotNull(connection1.poll());
        assertNotNull(connection2.poll());
    }

    @Test
    @DisplayName("경계값 처리")
    void boundaryValueTest() {
        Message message = new Message(Map.of("temperature", 30.0));

        splitNode.onProcess(message);

        assertNotNull(connection1.poll());
    }
}
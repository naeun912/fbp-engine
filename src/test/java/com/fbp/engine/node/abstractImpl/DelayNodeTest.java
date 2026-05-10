package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelayNodeTest {
    DelayNode delayNode;
    Connection connection;

    @BeforeEach
    void setUp() {
        delayNode = new DelayNode("delay - 1", 500);
        connection = new Connection("connection - 1");
        delayNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("지연 후 전달")
    void delayTest() {

        long startTime = System.currentTimeMillis();

        delayNode.onProcess(new Message(Map.of("key", 1)));

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        assertTrue(duration >= 500);
        assertTrue(duration < 600);
    }

    @Test
    @DisplayName("메세지 내용 보존")
    void messageTest() {
        Message message = new Message(Map.of("key", 1));
        delayNode.onProcess(message);

        assertEquals(1, (int) message.get("key"));
    }
}
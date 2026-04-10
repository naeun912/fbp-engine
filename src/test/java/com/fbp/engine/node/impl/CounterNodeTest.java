package com.fbp.engine.node.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterNodeTest {
    CounterNode counterNode;
    Connection connection;

    @BeforeEach
    void setUp() {
        counterNode = new CounterNode("counter - 1");
        connection = new Connection("connection - 1");
        counterNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("count 키 추가")
    void countKeyTest() {
        Message message = new Message(Map.of("key", 1));

        counterNode.onProcess(message);

        assertEquals(1, counterNode.getCount());

        Message result = connection.poll();

        assertEquals(1, (int) result.get("count"));
    }

    @Test
    @DisplayName("count 누적")
    void countTest() {
        Message message = new Message(Map.of("key", 1));
        Message message1 = new Message(Map.of("key", 2));
        Message message2 = new Message(Map.of("key", 3));

        counterNode.onProcess(message);
        counterNode.onProcess(message1);
        counterNode.onProcess(message2);

        assertEquals(3, counterNode.getCount());

        connection.poll();
        connection.poll();
        Message message3 = connection.poll();
        assertEquals(3, (int) message3.get("count"));
    }

    @Test
    @DisplayName("원본 키 유지")
    void messageTest() {
        Message message = new Message(Map.of("key", 1));

        counterNode.onProcess(message);

        assertEquals(1, (int) message.get("key"));
    }
}
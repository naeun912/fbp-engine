package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.GeneratorNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionBlockingQueueTest {
    Connection connection;
    Message message;
    GeneratorNode generatorNode;

    @BeforeEach
    void setUp() {
        connection = new Connection("connection - 1");
        generatorNode = new GeneratorNode("generate - 1");
        generatorNode.getOutputPort().connect(connection);
        generatorNode.generate("temperature", 25.5);
        message = connection.poll();
    }

    @Test
    @DisplayName("deliver-poll 기본 동작")
    void deliverPollTest() {
        connection.deliver(message);
        Message message1 = connection.poll();
        assertEquals(25.5, message1.get("temperature"));
    }

    @Test
    @DisplayName("메세지 순서 보장")
    void messageTestO() {
        Message message1 = new Message(Map.of("temperature", 25.5));
        Message message2 = new Message(Map.of("temperature", 25.6));
        Message message3 = new Message(Map.of("temperature", 25.7));

        connection.deliver(message1);
        connection.deliver(message2);
        connection.deliver(message3);

        assertEquals(message1, connection.poll());
        assertEquals(message2, connection.poll());
        assertEquals(message3, connection.poll());
    }

    @Test
    @DisplayName("멀티스레드 deliver - poll")
    void threadDeliverTest() {
        Thread thread1 = new Thread(() -> {

        });
    }
}
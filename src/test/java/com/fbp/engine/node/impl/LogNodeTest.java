package com.fbp.engine.node.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogNodeTest {
    LogNode logger;
    Message message;
    Connection connection;

    @BeforeEach
    void setUp() {
        logger = new LogNode("logger - 1");
        message = new Message(Map.of("temperature", 35.5));
        connection = new Connection("connection - 1");
    }

    @Test
    @DisplayName("메시지 통과 전달")
    void messagePassTest() {
        logger.getOutputPort("out").connect(connection);

        logger.onProcess(message);

        assertEquals(message, connection.poll());
    }
    
    @Test
    @DisplayName("중간 삽입 가능")
    void messageTest() {

        logger.getOutputPort("out").connect(connection);

        logger.onProcess(message);
        assertEquals(message, connection.poll());

        Message message2 = new Message(Map.of("status", "ok"));
        logger.onProcess(message2);

        assertEquals(message2, connection.poll());
    }
}
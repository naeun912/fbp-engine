package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ConnectionTest {
    private Connection connection;
    private Queue<Message> buffer;
    private InputPort inputPort;
    private Message message;

    @BeforeEach
    void setUp() {
        buffer = new LinkedList<>();

        inputPort = mock(InputPort.class);

        connection = new Connection("conn-1");

        message = new Message(Map.of("temperature", 25.5));
    }

    @Test
    @DisplayName("deliver 후 target 수신")
    void deliverTest() {
        connection.deliver(message);

        assertEquals(1, connection.getBufferSize());
    }

    @Test
    @DisplayName("target 미설정 시 동작")
    void targetNullTest() {
        inputPort = null;
        assertDoesNotThrow(() -> connection.deliver(message));
    }

    @Test
    @DisplayName("버퍼 크기 확인")
    void bufferSizeCheckTest() {
        connection.deliver(message);
        assertEquals(1, connection.getBufferSize());

//        connection.setTarget(null);
        connection.deliver(message);
        assertEquals(2, connection.getBufferSize());
    }

    @Test
    @DisplayName("다수 메세지 순서 보장")
    void messageTest() {
//        connection.setTarget(null);

        Message message1 = new Message(Map.of("temperature", 25.5));
        Message message2 = new Message(Map.of("temperature", 25.6));
        Message message3 = new Message(Map.of("temperature", 25.7));

        connection.deliver(message1);
        connection.deliver(message2);
        connection.deliver(message3);

        assertEquals(message1, connection.poll());
        assertEquals(message2, connection.poll());
        assertEquals(message3, connection.poll());

        assertTrue(buffer.isEmpty());

    }
}
package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.PrintNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DefaultOutputPortTest {
    private PrintNode printer;
    private Connection connection;
    private OutputPort outputPort;
    private Message message;

    @BeforeEach
    void setUp() {
        Map<String, Object> payload = Map.of("temperature", 25.5);
        message = new Message(payload);
        printer = new PrintNode("print-1");
        outputPort = new DefaultOutputPort();
    }

    @Test
    @DisplayName("단일 connection 전달")
    void oneConnectionTest() {
        Connection mockConn = mock(Connection.class);

        outputPort.connect(mockConn);

        outputPort.send(message);

        verify(mockConn, times(1)).deliver(message);
    }

    @Test
    @DisplayName("대중 connection 전달 (1:N)")
    void connectionTEst() {
        Connection mockConn = mock(Connection.class);
        Connection mockConn2 = mock(Connection.class);

        outputPort.connect(mockConn);
        outputPort.connect(mockConn2);

        outputPort.send(message);

        verify(mockConn, times(1)).deliver(message);
        verify(mockConn2, times(1)).deliver(message);
    }

    @Test
    @DisplayName("Connection 미연결 시")
    void notConnectionTest() {
        assertDoesNotThrow(() -> outputPort.send(message));
    }
}
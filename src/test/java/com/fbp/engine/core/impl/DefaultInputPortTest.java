package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.InputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.PrintNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultInputPortTest {
    private PrintNode printNode;
    private InputPort inputPort;
    private Connection mockConnection; // 👈 커넥션 추가

    @BeforeEach
    void setUp() {
        printNode = new PrintNode("printer-1");
        inputPort = new DefaultInputPort(printNode, "in");
        mockConnection = mock(Connection.class); // 👈 Mock 생성
    }

    @Test
    @DisplayName("receive 시 connection의 poll 호출 확인")
    void ownerTest() {
        Message msg = new Message(Map.of("temperature", 25.5));

        when(mockConnection.poll()).thenReturn(msg);

        ((DefaultInputPort) inputPort).setConnection(mockConnection);

        Message result = inputPort.receive();

        verify(mockConnection, times(1)).poll();
        assertEquals(msg, result);
    }

    @Test
    @DisplayName("포트 이름 확인")
    void returnNameTest() {
        assertEquals("in", inputPort.getName());
    }
}
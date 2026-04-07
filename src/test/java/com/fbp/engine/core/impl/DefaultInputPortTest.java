package com.fbp.engine.core.impl;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.PrintNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultInputPortTest {
    private Message message;
    private PrintNode printNode;
    private InputPort inputPort;

    @BeforeEach
    void setUp() {
        Map<String, Object> payload = Map.of("temperature", 25.5);
        message = new Message(payload);
        printNode = new PrintNode("printer-1");
        inputPort = new DefaultInputPort(printNode);
    }

    @Test
    @DisplayName("receive 시 owner 호출")
    void ownerTest() {
        PrintNode spyPrinter = spy(printNode);
        inputPort = new DefaultInputPort(spyPrinter);
        Message msg = new Message(Map.of("temperature", 25.5));

        inputPort.receive(msg);

        verify(spyPrinter, times(1)).process(msg);
    }

    @Test
    @DisplayName("포트 이름 확인")
    void returnNameTest() {
        assertEquals("in", inputPort.getName());
    }
}
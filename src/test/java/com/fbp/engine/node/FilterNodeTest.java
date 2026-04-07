package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class FilterNodeTest {
    FilterNode filter;
    Connection connection;
    OutputPort outputPort;

    @BeforeEach
    void setUp() {
        filter = new FilterNode("filter-1", "temperature", 30.0);
        connection = new Connection(new LinkedList<>(), filter.getInputPort(), "connection-filter");
        outputPort = mock(OutputPort.class);
        filter.setOutputPort(outputPort);
    }

    @Test
    @DisplayName("조건 만족 시 통과")
    void trueTest() {
        Message message = new Message(Map.of("temperature", 35.5));
        filter.process(message);
        verify(outputPort, times(1)).send(message);
    }

    @Test
    @DisplayName("조건 미달 시 차단")
    void falseTest() {
        Message message = new Message(Map.of("temperature", 25.5));
        filter.process(message);

        verify(outputPort, never()).send(message);
    }

    @Test
    @DisplayName("경계값 처리")
    void thresholdEqualTest() {
        Message message = new Message(Map.of("temperature", 30.0));
        filter.process(message);

        verify(outputPort, times(1)).send(message);
    }

    @Test
    @DisplayName("키 없는 메세지")
    void noKeyMessageTest() {
        Message message = new Message(Map.of("humidity", 30.0));

        assertDoesNotThrow(() -> filter.process(message));
        verify(outputPort, never()).send(message);
    }
}
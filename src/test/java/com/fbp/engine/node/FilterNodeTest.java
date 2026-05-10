package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.FilterNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilterNodeTest {
    FilterNode filter;
    Connection connection;
    OutputPort outputPort;
    Message message;

    @BeforeEach
    void setUp() {
        filter = new FilterNode("filter-1", "temperature", 30.0);

        outputPort = mock(OutputPort.class);

        filter.getOutputPorts().put("out", outputPort);

        connection = new Connection("connection-1");
        message = new Message(Map.of("temperature", 35.5));
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

    @Test
    @DisplayName("조건 만족 → send 호출")
    void thresholdOutputPortTest() {
//        filter.getOutputPort("out").connect(connection);
//        filter.send("out", message);
//
//        assertEquals(1, connection.getBufferSize());
//
//        Message received = connection.poll();
//        assertNotNull(received);

        filter.addOutputPort("out");

        // 2. 이제 진짜 포트에 연결
        filter.getOutputPort("out").connect(connection);

        // 3. 메시지 발송
        filter.send("out", message);

        // 4. 이제 1이 나올 겁니다!
        assertEquals(1, connection.getBufferSize());
    }

    @Test
    @DisplayName("조건 미달 → 차단")
    void thresholNotdOutputPortTest() {
        Message message1 = new Message(Map.of("temperature", 25.5));
        filter.getOutputPort("out").connect(connection);
        filter.onProcess(message1);

        assertEquals(0, connection.getBufferSize());
    }

    @Test
    @DisplayName("포트 구성 확인")
    void portTest() {
        assertNotNull(filter.getOutputPort("out"));
        assertNotNull(filter.getInputPort("in"));
    }
}
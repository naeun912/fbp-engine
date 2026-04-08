package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractNodeTest {
    private final String TEST_ID = "test-node";
    AbstractNode node;
    Message message;
    private boolean onProcessCalled = false;

    @BeforeEach
    void setUp() {
        onProcessCalled = false; // 테스트마다 초기화
        message = new Message(Map.of("temperature", 25.5));
        // AbstractNode를 상속받은 익명 클래스나 테스트 전용 클래스로 생성
        node = new AbstractNode(TEST_ID) {
            @Override
            public void onProcess(Message message) {
                onProcessCalled = true; // 호출 여부 확인용
            }

            @Override
            public void initialize() {
            }

            @Override
            public void shutdown() {
            }
        };
    }

    @Test
    @DisplayName("getID 반환")
    void getIdTest() {
        assertEquals(TEST_ID, node.getId());
    }

    @Test
    @DisplayName("addInputPort 등록")
    void addInputPortTest() {
        node.addInputPort("in");
        assertNotNull(node.getInputPort("in"));
    }

    @Test
    @DisplayName("addOutputPort 등록")
    void addOutputPortTest() {
        node.addOutputPort("out");
        assertNotNull(node.getOutputPort("out"));
    }

    @Test
    @DisplayName("미등록 포트 조회")
    void notPortTest() {
        assertNull(node.getInputPort("innn"));
    }

    @Test
    @DisplayName("process → onProcess 호출")
    void onProcessTest() {
        node.process(message);

        assertTrue(onProcessCalled);
    }

    @Test
    @DisplayName("send로 메세지 전달")
    void sendMessageTest() {
        Connection connection = new Connection("connection - 1");
        node.addOutputPort("out");
        node.getOutputPort("out").connect(connection);
        node.send("out", message);

        Message receive = connection.poll();

        assertNotNull(receive);
        assertEquals(message, receive);
    }
}
package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.impl.PrintNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GeneratorNodeTest {
    GeneratorNode generatorNode;
    Message message;
    PrintNode printer;
    OutputPort outputPort;
    Connection connection;

    @BeforeEach
    void setUp() {
        generatorNode = new GeneratorNode("generate - 1");
        connection = mock(Connection.class);
        outputPort = new DefaultOutputPort();
        generatorNode.getOutputPort().connect(connection);
    }

    @Test
    @DisplayName("generate 메세지 생성")
    void generateMessageTest() {

        generatorNode.generate("temperature", 25.5);

        verify(connection, times(1)).deliver(any(Message.class));
    }

    @Test
    @DisplayName("메세지 내용 확인")
    void MessageCheckTest() {

        generatorNode.generate("temperature", 35.5);

        // 전달된 메시지의 페이로드를 그 자리에서 바로 검사하는 역할 >> argThat
        verify(connection).deliver(argThat(msg ->
                msg.payload().get("temperature").equals(35.5)
        ));

    }

    @Test
    @DisplayName("OutputPort 조회")
    void outputPortTest() {
        assertNotNull(generatorNode.getOutputPort());
    }

    @Test
    @DisplayName("다수 generate 호출")
    void manyGenerateTest() {

        generatorNode.generate("temperature", 25.5);
        generatorNode.generate("temperature", 25.6);
        generatorNode.generate("temperature", 25.7);
        // 순서 감시자 >> InOrder
        InOrder inOrder = inOrder(connection);


        inOrder.verify(connection).deliver(argThat(msg -> msg.payload().get("temperature").equals(25.5)));
        inOrder.verify(connection).deliver(argThat(msg -> msg.payload().get("temperature").equals(25.6)));
        inOrder.verify(connection).deliver(argThat(msg -> msg.payload().get("temperature").equals(25.7)));

        verify(connection, times(3)).deliver(any(Message.class));
    }
}
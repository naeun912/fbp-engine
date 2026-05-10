package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.PrintNode;
import com.fbp.engine.node.abstractImpl.TimerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PrintNodeTest {
    private Message message;
    private PrintNode printNode;

    @BeforeEach
    void setUp() {
        Map<String, Object> payload = Map.of("temperature", 25.5);
        message = new Message(payload);
        printNode = new PrintNode("printer-1");
    }

    @Test
    @DisplayName("getId 반환")
    void getIdTest() {
        assertEquals("printer-1", printNode.getId());
    }

    @Test
    @DisplayName("process 정상 동작")
    void processTest() {
        assertDoesNotThrow(() -> printNode.process(message));
    }

    @Test
    @DisplayName("Node 인터페이스 구현")
    void nodeInterfaceTest() {
        Node node = printNode;

        assertNotNull(node);
    }

    @Test
    @DisplayName("inputPort 조회")
    void inputPortNotNullTest() {
        assertNotNull(printNode.getInputPort("in"));
    }

    @Test
    @DisplayName("inputPort를 통한 수신")
    void inputPortTest() throws InterruptedException {
        FlowEngine flowEngine = new FlowEngine();
        Flow flow = new Flow("flow");
        TimerNode timerNode = new TimerNode("timer", 1000);
        flow.addNode(timerNode).addNode(printNode)
                .connect("timer", "out", "printer-1", "in");
        flowEngine.register(flow);
        flowEngine.startFlow("flow");
        Thread.sleep(2000);
        flowEngine.stopFlow("flow");
    }

    @Test
    @DisplayName("포트 구성 확인")
    void portTest() {
        printNode.addInputPort("in");
        assertNotNull(printNode.getInputPort("in"));
    }

    @Test
    @DisplayName("process 정상 동작")
    void processNotExceptionTest() {
        assertDoesNotThrow(() -> printNode.onProcess(message));

    }

    @Test
    @DisplayName("AbstractNode 상속 확인")
    void abstractNodeInstanceofTest() {
        assertTrue(printNode instanceof AbstractNode);
    }
}


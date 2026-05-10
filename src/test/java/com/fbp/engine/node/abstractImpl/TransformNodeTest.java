package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransformNodeTest {
    TransformNode transformer;
    Connection connection;

    @BeforeEach
    void setUp() {
        connection = new Connection("connection - 1");
        transformer = new TransformNode("transformer - 1", message -> {
            double f = (double) message.get("temp");
            double c = (f - 32) * 5 / 9;
            return new Message(Map.of("temp", c));
        });
        transformer.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("변환 정상 동작")
    void transformerOutputTest() {
        Message message = new Message(Map.of("temp", 100.0));

        transformer.onProcess(message);


        Message message1 = connection.poll();
        assertEquals(37.77777777777778, (double) message1.get("temp"), 0.00001);
    }

    @Test
    @DisplayName("null 반환 시 미전달")
    void nullReturnTest() {
        TransformNode transformer1 = new TransformNode("transformer - 2", message -> null);
        transformer1.getOutputPort("out").connect(connection);
        transformer1.onProcess(new Message(Map.of("temp", 100)));
        Message message = null;
        try {
            message = connection.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(message);
    }

    @Test
    @DisplayName("원본 메시지 불변")
    void messageTest() {
        Message message = new Message(Map.of("temp", 100.0));

        transformer.onProcess(message);

        assertEquals(100.0, message.get("temp"));

    }

}
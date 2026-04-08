package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.impl.GeneratorNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionBlockingQueueTest {
    Connection connection;
    Message message;
    GeneratorNode generatorNode;
    CountDownLatch latch;
    AtomicReference<Message> result;

    @BeforeEach
    void setUp() {
        connection = new Connection("connection - 1");
        generatorNode = new GeneratorNode("generate - 1");
        generatorNode.getOutputPort("out").connect(connection);
        message = new Message(Map.of("temperature", 25.5));
        latch = new CountDownLatch(1);
        result = new AtomicReference<>();
    }

    @Test
    @DisplayName("deliver-poll 기본 동작")
    void deliverPollTest() {
        connection.deliver(message);
        Message message1 = connection.poll();
        assertEquals(25.5, message1.get("temperature"));
    }

    @Test
    @DisplayName("메세지 순서 보장")
    void messageTestO() {
        Message message1 = new Message(Map.of("temperature", 25.5));
        Message message2 = new Message(Map.of("temperature", 25.6));
        Message message3 = new Message(Map.of("temperature", 25.7));

        connection.deliver(message1);
        connection.deliver(message2);
        connection.deliver(message3);

        assertEquals(message1, connection.poll());
        assertEquals(message2, connection.poll());
        assertEquals(message3, connection.poll());
    }

    @Test
    @DisplayName("멀티스레드 deliver - poll")
    void threadDeliverTest() {
        Thread thread = new Thread(() -> {
            connection.deliver(message);
            latch.countDown();
        });
        Thread thread1 = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Message r = connection.poll();
            result.set(r);
        });

        thread.start();
        thread1.start();

        try {
            thread.join();
            thread1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(message, result.get());
    }

    @Test
    @DisplayName("poll 대기 동작")
    void pollWaitTest() {
        Thread thread = new Thread(() -> {
            try {
                connection.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(3000);
                connection.deliver(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
        thread1.start();

        // 시간이 4초가 지나도 종료 안되면 강제종료!!!
        assertTimeout(Duration.ofSeconds(4), () -> {
            thread.join();
            thread1.join();
        });
    }

    @Test
    @DisplayName("버퍼 크기 제한")
    void bufferSizeTest() {
        Message message1 = new Message(Map.of("temperature", 25.5));
        Message message2 = new Message(Map.of("temperature", 25.6));
        Message message3 = new Message(Map.of("temperature", 25.7));
        connection = new Connection("connection - 2", 2);

        Thread thread = new Thread(() -> {
            connection.deliver(message1);
            connection.deliver(message2);
            connection.deliver(message3);
        });

        thread.start();

        try {
            Thread.sleep(500);
            assertNotEquals(Thread.State.TERMINATED, thread.getState());
            connection.poll();
            thread.join(1000);
            assertEquals(Thread.State.TERMINATED, thread.getState());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("버퍼 크기 조회")
    void bufferSizeCheckTest() {
        connection.deliver(message);
        connection.deliver(message);
        assertEquals(2, connection.getBufferSize());
    }

}
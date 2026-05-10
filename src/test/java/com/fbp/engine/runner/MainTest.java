package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.abstractImpl.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class MainTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode tempNode = new TemperatureSensorNode("temp", 25, 35);
        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30);
        FileWriterNode fileWriterNode = new FileWriterNode("file", FileWriterNode.FILE_PATH);
        CollectorNode alertCollector = new CollectorNode("alertCollector");
        CollectorNode normalCollector = new CollectorNode("normalCollector");

        flow.addNode(timerNode).addNode(tempNode).addNode(filterNode)
                .addNode(alertCollector).addNode(normalCollector).addNode(fileWriterNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "filter", "in")
                .connect("filter", "alert", "alertCollector", "in")
                .connect("filter", "normal", "normalCollector", "in")
                .connect("filter", "normal", "file", "in");

        engine.register(flow);
        engine.startFlow("flow");
        // 1. 엔진 시작/종료
        assertEquals(Flow.State.RUNNING, flow.getState());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 6. 센서 데이터 형식
        Connection conn1 = flow.getConnections().get(2);
        Message msg2 = conn1.poll();

        assertNotNull(msg2);
        assertTrue(msg2.hasKey("sensorId"));
        assertTrue(msg2.hasKey("unit"));
        assertTrue(msg2.hasKey("temperature"));

        // 7. 온도 범위
        Connection conn = flow.getConnections().get(1);
        Message msg1 = conn.poll();

        assertNotNull(msg1);
        double temp = msg1.get("temperature");

        assertTrue(temp >= 15 && temp <= 45);

        engine.stopFlow("flow");

        // 1. 엔진 시작/종료
        assertEquals(Flow.State.STOPPED, flow.getState());

        // 2. alert 경로 정확성
        for (Message msg : alertCollector.getCollected()) {
            double val = msg.get("temperature");
            assertTrue(val > 30);
        }

        // 3. normal 경로 정확성
        for (Message msg : normalCollector.getCollected()) {
            double val = msg.get("temperature");
            assertTrue(val <= 30);
        }

        // 4. 전체 분기 완전성
        int alertCount = alertCollector.getCollected().size();
        int normalCount = normalCollector.getCollected().size();
        int totalCollected = alertCount + normalCount;

        assertTrue(totalCollected >= 9 && totalCollected <= 20);

        assertFalse(alertCollector.getCollected().isEmpty());
        assertFalse(normalCollector.getCollected().isEmpty());

        // 5. 파일 기록 검증
        Path path = Paths.get(FileWriterNode.FILE_PATH);
        long lineCount = Files.readAllLines(path).size();

        assertEquals(normalCount, lineCount);

    }
}

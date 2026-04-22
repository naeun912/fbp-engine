package com.fbp.engine.node.impl;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWriterNodeTest {
    FileWriterNode writerNode;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        writerNode = new FileWriterNode("fileWriter", "/Users/naeun/IdeaProjects/fbp-engine/fileWriter");
        flow = new Flow("flow");
        engine = new FlowEngine();
    }

    @Test
    @DisplayName("파일 생성")
    void fileCreateTest() throws InterruptedException {
        TimerNode timer = new TimerNode("timer", 500);
        TemperatureSensorNode temp = new TemperatureSensorNode("temp", 20, 30);
        String pathString = "/Users/naeun/IdeaProjects/fbp-engine/fileWriter";
        PrintNode printNode = new PrintNode("printer");
        Path path = Paths.get(pathString);


        flow.addNode(timer).addNode(temp).addNode(writerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect(temp.getId(), "out", writerNode.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");

        Thread.sleep(4000);
        engine.stopFlow("flow");
        Thread.sleep(1000);
        assertTrue(Files.exists(path));

        System.out.println("파일 생성 확인 완료: " + path.toAbsolutePath());
    }

    @Test
    @DisplayName("내용 기록")
    void fileReadTest() throws InterruptedException, IOException {
        TimerNode timer = new TimerNode("timer", 500);
        TemperatureSensorNode temp = new TemperatureSensorNode("temp", 20, 30);
        String pathString = "/Users/naeun/IdeaProjects/fbp-engine/fileWriter";
        PrintNode printNode = new PrintNode("printer");
        Path path = Paths.get(pathString);


        flow.addNode(timer).addNode(temp).addNode(writerNode)
                .connect("timer", "out", "temp", "trigger")
                .connect(temp.getId(), "out", writerNode.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");

        Thread.sleep(4000);
        engine.stopFlow("flow");
        Thread.sleep(1000);
        assertTrue(Files.exists(path));

        System.out.println("파일 생성 확인 완료: " + path.toAbsolutePath());

        List<String> lines = Files.readAllLines(path);

        System.out.println("파일 내용: " + lines);

        assertFalse(lines.isEmpty());
    }

    @Test
    @DisplayName("shutdown 후 파일 닫힘")
    void shutDownFileCloseTest() throws IOException {
        String pathString = "/Users/naeun/IdeaProjects/fbp-engine/fileWriter";
        TimerNode timer = new TimerNode("timer", 500);
        Path path = Paths.get(pathString);

        flow.addNode(writerNode).addNode(timer).connect("timer", "out", writerNode.getId(), "in");
        engine.register(flow);
        engine.startFlow("flow");

        writerNode.shutdown();

        Message extraMsg = new Message(java.util.Map.of("data", "after shutdown"));

        writerNode.onProcess(extraMsg);

        List<String> lines = Files.readAllLines(path);
        boolean containsExtraMsg = lines.stream().anyMatch(line -> line.contains("after shutdown"));

        assertFalse(containsExtraMsg, "shutdown 이후의 메시지가 파일에 기록되면 안 됩니다!");
    }

}
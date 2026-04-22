package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterNode extends AbstractNode {
    public static final String FILE_PATH = "/Users/naeun/IdeaProjects/fbp-engine/fileWriter";
    private final String filePath;
    private BufferedWriter writer;

    public FileWriterNode(String id, String filePath) {
        super(id);
        this.filePath = filePath;
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        String msg = message.toString();
        try {
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("[" + getId() + "] 파일 기록 중 에러 발생: " + e.getMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            writer = new BufferedWriter(new FileWriter(filePath, true));
        } catch (IOException e) {
            System.err.println("[" + getId() + "] 파일 여는 중 에러 발생: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("[" + getId() + "] 파일 닫는 중 에러 발생: " + e.getMessage());
        }
    }
}

package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.Map;

public class GeneratorNode extends AbstractNode {

    public GeneratorNode(String id) {
        super(id);
        addOutputPort("out");
    }

    /**
     * 데이터를 생성해서 "out" 포트로 쏴주는 메서드
     * 과제 6-2에서 화씨 온도를 생성할 때 이 메서드를 호출하면 됩니다.
     */
    public void generate(String key, Object value) {
        Message message = new Message(Map.of(key, value));
        send("out", message);
        System.out.println("[" + getId() + "] 📤 데이터 생성: " + key + " = " + value);
    }

    @Override
    public void onProcess(Message message) {

    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {

    }
}
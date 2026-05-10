package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.Map;

public class GeneratorNode extends AbstractNode {

    public GeneratorNode(String id) {
        super(id);
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
    }

    public void generate(String key, Object value) {
        Message newMessage = new Message(Map.of(key, value));
        send("out", newMessage);
        System.out.println("[" + getId() + "] 메시지 생성 및 전송: " + key + " = " + value);
    }
}
package com.fbp.engine.node.abstractImpl;


import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

// 테스트를 위한 코드
public class DummySourceNode extends AbstractNode {
    public DummySourceNode(String id) {
        super(id);
        addOutputPort("out"); // 포트만 만들어둠
    }

    @Override
    public void onProcess(Message message) {
        send("out", message);
    }
}

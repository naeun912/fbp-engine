package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class CollectorNode extends AbstractNode {
    //    private final List<Message> collected = new ArrayList<>();
    private final List<Message> collected = new CopyOnWriteArrayList<>();

    public CollectorNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        collected.add(message);
        System.out.println("[" + getId() + "] 결과 | " + message.payload());
    }

}

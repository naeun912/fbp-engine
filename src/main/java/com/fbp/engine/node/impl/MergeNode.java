package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class MergeNode extends AbstractNode {
    private Message pending1;
    private Message pending2;

    public MergeNode(String id) {
        super(id);
        addInputPort("in-1");
        addInputPort("in-2");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        String portName = message.get("_portName");
        System.out.println("[MergeNode] " + portName + " 포트로 메시지 들어옴!");
        if ("in-1".equals(portName)) {
            pending1 = message;
        } else if ("in-2".equals(portName)) {
            pending2 = message;
        }

        if (pending1 != null && pending2 != null) {
            Message mergedMessage = pending1.withPayload(pending2.payload());
            send("out", mergedMessage);

            pending1 = null;
            pending2 = null;
        }
    }
}

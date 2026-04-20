package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class AlertNode extends AbstractNode {
    public AlertNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        
    }
}

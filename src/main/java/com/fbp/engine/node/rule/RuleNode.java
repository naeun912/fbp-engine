package com.fbp.engine.node.rule;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.function.Predicate;

public class RuleNode extends AbstractNode {
    private Predicate<Message> condition;

    public RuleNode(String id, Predicate<Message> condition) {
        super(id);
        this.condition = condition;
        addOutputPort("match");
        addOutputPort("mismatch");
        addInputPort("in");
    }

    public RuleNode(String id, String expression) {
        this(id, (message) -> RuleExpression.parse(expression).evaluate(message));
    }

    @Override
    public void onProcess(Message message) {
        if (condition.test(message)) {
            send("match", message);
        } else {
            send("mismatch", message);
        }
    }
}

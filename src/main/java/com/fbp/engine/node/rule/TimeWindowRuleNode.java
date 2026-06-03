package com.fbp.engine.node.rule;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;

public class TimeWindowRuleNode extends AbstractNode {
    private final Predicate<Message> condition;
    private final long windowMs;
    private final int threshold;
    private final Queue<Long> events; // Using millis for math

    public TimeWindowRuleNode(String id, Predicate<Message> cond,
                              long ms, int limit) {
        super(id);
        this.condition = cond;
        this.windowMs = ms;
        this.threshold = limit;
        this.events = new LinkedList<>();

        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("pass");
    }


    @Override
    public void onProcess(Message message) {
        long now = System.currentTimeMillis();

        if (condition.test(message)) {
            events.add(now);
        }

        // (현재 시간 - Queue 맨 앞의 시간 > windowMs)인 녀석들을 while문으로 돌면서 poll로 꺼내어 지워버린다.
        while (!events.isEmpty() && (now - events.peek()) > windowMs) {
            events.poll();
        }

        if (events.size() >= threshold) {
            send("alert", message);
        } else {
            send("pass", message);
        }
    }
}

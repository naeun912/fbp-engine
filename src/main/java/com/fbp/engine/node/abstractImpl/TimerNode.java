package com.fbp.engine.node.abstractImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class TimerNode extends AbstractNode {
    private long intervalMs;
    private int tickCount = 0;
    private ScheduledExecutorService scheduler;

    public TimerNode(String id, long intervalMs) {
        super(id);
        this.intervalMs = intervalMs;
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {

    }

    @Override
    public void initialize() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            scheduler.scheduleAtFixedRate(() -> {
                tickCount++;
                Message message = new Message(Map.of(
                        "tick", tickCount,
                        "timestamp", System.currentTimeMillis()
                ));

                send("out", message);

//            System.out.println("[" + getId() + "] 🕑tick : " + tickCount);

            }, 0, intervalMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            System.out.println("[" + getId() + "] shutdown.");
        }
    }
}

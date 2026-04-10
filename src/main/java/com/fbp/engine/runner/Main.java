package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import com.fbp.engine.node.impl.PrintNode;
import com.fbp.engine.node.impl.SplitNode;
import com.fbp.engine.node.impl.TimerNode;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) {
        Flow flow = new Flow("temp-pipeline");

        TimerNode timer = new TimerNode("timer", 1000);
        SplitNode splitter = new SplitNode("splitter", "tick", 3);
        PrintNode warningPrint = new PrintNode("matchPrint");
        PrintNode normalPrint = new PrintNode("mismatchPrint");

        flow.addNode(timer)
                .addNode(splitter)
                .addNode(warningPrint)
                .addNode(normalPrint)
                .connect("timer", "out", "splitter", "in")
                .connect("splitter", "match", "matchPrint", "in")
                .connect("splitter", "mismatch", "mismatchPrint", "in");

        startWorker(flow);
        flow.initialize();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        timer.shutdown();
    }

    private static void startWorker(Flow flow) {
        for (Connection conn : flow.getConnections()) {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        Message m = conn.poll();
                        if (m != null) {
                            String targetNodeId = conn.getId().split("->")[1].split(":")[0];
                            String targetPort = conn.getId().split("->")[1].split(":")[1];

                            AbstractNode nextNode = flow.getNodes().get(targetNodeId);
                            nextNode.getInputPort(targetPort).receive(m);
                        }
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
}

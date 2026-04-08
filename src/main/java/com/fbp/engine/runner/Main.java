package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
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
        TimerNode timer = new TimerNode("timer-1", 500);

        SplitNode splitter = new SplitNode("split-1", "tick", 3.0);

        PrintNode warningPrint = new PrintNode("⚠️ 경고");
        PrintNode normalPrint = new PrintNode("✅ 정상");


        Connection c_in = new Connection("c_in");
        Connection c_match = new Connection("c_match");
        Connection c_mismatch = new Connection("c_mismatch");


        timer.getOutputPort("out").connect(c_in);
        splitter.getOutputPort("match").connect(c_match);
        splitter.getOutputPort("mismatch").connect(c_mismatch);


        startWorker(c_in, splitter);
        startWorker(c_match, warningPrint);
        startWorker(c_mismatch, normalPrint);


        timer.initialize();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        timer.shutdown();
    }

    private static void startWorker(Connection conn, AbstractNode nextNode) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message m = conn.poll();
                    if (m != null) {
                        nextNode.getInputPort("in").receive(m);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}

package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
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

        flow.initialize();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        flow.shutdown();
    }
}

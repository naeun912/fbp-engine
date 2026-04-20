package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.impl.PrintNode;
import com.fbp.engine.node.impl.TimerNode;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flowA = new Flow("FlowA");
        flowA.addNode(new TimerNode("timerA", 3000))
                .addNode(new PrintNode("printA"))
                .connect("timerA", "out", "printA", "in");

        Flow flowB = new Flow("FlowB");
        flowB.addNode(new TimerNode("timerB", 5000))
                .addNode(new PrintNode("printB"))
                .connect("timerB", "out", "printB", "in");

        engine.register(flowA);
        engine.register(flowB);

        engine.startCLI();
    }

}

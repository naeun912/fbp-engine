package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.impl.*;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");
        TimerNode timerNode = new TimerNode("timer", 1000);
        TemperatureSensorNode temperatureSensorNode = new TemperatureSensorNode("temp", 15, 45);
        ThresholdFilterNode thresholdFilterNode = new ThresholdFilterNode("threshold", "temperature", 30);
        ThresholdFilterNode thresholdFilterNode1 = new ThresholdFilterNode("threshold", "humidity", 70);
        AlertNode alertNode = new AlertNode("alert");
        LogNode logNode = new LogNode("logger");
        PrintNode printNode = new PrintNode("printer");

        flow.addNode(timerNode)
                .addNode(temperatureSensorNode)
                .addNode(thresholdFilterNode)
                .addNode(thresholdFilterNode1)
                .addNode(alertNode)
                .addNode(logNode)
                .addNode(printNode)
                .connect("timer", "out", "temp", "trigger")
                .connect("temp", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "logger", "in")
                .connect("logger", "out", "printer", "in");
        engine.register(flow);
//        engine.startCLI();
        engine.startFlow("flow");
        Thread.sleep(10000);
        engine.stopFlow("flow");

    }

}

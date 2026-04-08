package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.impl.FilterNode;
import com.fbp.engine.node.impl.PrintNode;
import com.fbp.engine.node.impl.TimerNode;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) {
        TimerNode timer = new TimerNode("timer-1", 500);
        FilterNode filter = new FilterNode("filter-1", "tick", 3.0);
        PrintNode printer = new PrintNode("printer-1");

        Connection conn1 = new Connection("connection - 1");
        Connection conn2 = new Connection("connection - 2");

        timer.getOutputPort("out").connect(conn1);
        filter.getOutputPort("out").connect(conn2);

        timer.initialize();
        filter.initialize();
        printer.initialize();

        Thread thread1 = new Thread(() -> {
            try {
                while (true) {
                    Message message1 = conn1.poll();
                    filter.getInputPort("in").receive(message1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread1.setDaemon(true);
        thread1.start();

        Thread thread2 = new Thread(() -> {
            try {
                while (true) {
                    Message message2 = conn2.poll();
                    printer.getInputPort("in").receive(message2);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread2.setDaemon(true);
        thread2.start();

        try {
            Thread.sleep(3000);
            timer.shutdown();
            filter.shutdown();
            printer.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

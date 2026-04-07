package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

/**
 * Hello world!
 */
public class Main {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        GeneratorNode generator = new GeneratorNode("generator-1");
        FilterNode filter = new FilterNode("filter-1", "temperature", 30.0);
        PrintNode printer = new PrintNode("print-1");

        Connection connection1 = new Connection("connection - 1", 10);
        Connection connection2 = new Connection("connection - 2", 10);

        generator.getOutputPort().connect(connection1);
        filter.getOutputPort().connect(connection2);

        Thread producerThread = new Thread(() -> {
            for (double i = 28.5; i < 50.5; i++) {
                generator.generate("temperature", i);
                System.out.println("버퍼 확인 1 : " + connection1.getBufferSize());
                System.out.println("버퍼 확인 2 : " + connection2.getBufferSize());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread filterThread = new Thread(() -> {
            while (running) {
                Message message = connection1.poll();
                if (message != null) {
                    filter.process(message);
                }
            }
        });

        Thread consumerThread = new Thread(() -> {
            while (running) {
                Message message = connection2.poll();

                if (message != null) {
                    printer.process(message);
                    System.out.println("버퍼 확인 1 : " + connection1.getBufferSize());
                    System.out.println("버퍼 확인 2 : " + connection2.getBufferSize());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        consumerThread.setDaemon(true);
        filterThread.setDaemon(true);
        producerThread.start();
        filterThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            running = false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("모든 thread 종료");
    }
}

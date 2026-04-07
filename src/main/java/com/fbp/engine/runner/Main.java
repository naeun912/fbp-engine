package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

import java.util.LinkedList;

/**
 * Hello world!
 */
public class Main {
    public static void main(String[] args) {
        GeneratorNode generator = new GeneratorNode("generator-1");
        FilterNode filter = new FilterNode("filter-1", "temperature", 30.0);
        PrintNode printer = new PrintNode("print-1");

        Connection conn1 = new Connection(new LinkedList<>(), filter.getInputPort(), "conn-gen-to-filter");
        generator.getOutputPort().connect(conn1);

        Connection conn2 = new Connection(new LinkedList<>(), printer.getInputPort(), "conn-filter-to-print");
        filter.getOutputPort().connect(conn2);

        generator.generate("temperature", 25.5);

        generator.generate("temperature", 35.5);
    }
}

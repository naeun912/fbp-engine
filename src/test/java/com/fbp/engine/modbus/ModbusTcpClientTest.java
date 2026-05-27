package com.fbp.engine.modbus;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

class ModbusTcpClientTest {

    ModbusTcpClient client;
    Flow flow;
    FlowEngine engine;

    @BeforeEach
    void setUp() {
        Map<String, Object> config = new HashMap<>();

        flow = new Flow("flow");
        engine = new FlowEngine();
    }
}
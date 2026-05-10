package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolNodeTest {
    ProtocolNode protocolNode;
    Flow flow;
    FlowEngine engine;


    @BeforeEach
    void setUp() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 8080);
        config.put("maxRetries", 5);
        protocolNode = new ProtocolNode("test", config) {
            @Override
            public void connect() throws IOException {

            }

            @Override
            public void disconnect() {

            }

            @Override
            protected byte[] readData() throws IOException {
                return new byte[0];
            }
        };
    }

    @Test
    @DisplayName("초기 상태")
    void setStateTest() {
        assertEquals(ProtocolNode.ConnectionState.DISCONNECTED, protocolNode.getConnectionState());
    }

    @Test
    @DisplayName("config 조회")
    void getConfigTest() {
        int port = (int) protocolNode.getConfig("port");
        String host = (String) protocolNode.getConfig("host");

        assertEquals(8080, port);
        assertEquals("localhost", host);
    }

    @Test
    @DisplayName("initialize → CONNECTED")
    void initializeStateConnectedTest() {
        protocolNode.initialize();

        assertEquals(ProtocolNode.ConnectionState.CONNECTED, protocolNode.getConnectionState());
    }

    @Test
    @DisplayName("initialize → 연결 실패 시 상태")
    void initializeStateErrorTest() {
        ProtocolNode failNode = new ProtocolNode("fail-test", new HashMap<>()) {
            @Override
            public void connect() throws IOException {
                throw new IOException("연결 거부됨!");
            }

            @Override
            public void disconnect() {
            }

            @Override
            protected byte[] readData() throws IOException {
                return null;
            }
        };

        failNode.initialize();

        assertEquals(ProtocolNode.ConnectionState.ERROR, failNode.getConnectionState());
    }

    @Test
    @DisplayName("shutdown → DISCONNECTED")
    void shutdownDisconnectedTest() {
        assertEquals(ProtocolNode.ConnectionState.DISCONNECTED, protocolNode.getConnectionState());

        protocolNode.initialize();

        assertEquals(ProtocolNode.ConnectionState.CONNECTED, protocolNode.getConnectionState());

        protocolNode.shutdown();

        assertEquals(ProtocolNode.ConnectionState.DISCONNECTED, protocolNode.getConnectionState());
    }

    @Test
    @DisplayName("isConnected 반환값")
    void isConnectedTest() {
        assertEquals(ProtocolNode.ConnectionState.DISCONNECTED, protocolNode.getConnectionState());
        assertFalse(protocolNode.isConnected());
        protocolNode.initialize();

        assertEquals(ProtocolNode.ConnectionState.CONNECTED, protocolNode.getConnectionState());
        assertTrue(protocolNode.isConnected());

        protocolNode.shutdown();
        assertFalse(protocolNode.isConnected());
        assertEquals(ProtocolNode.ConnectionState.DISCONNECTED, protocolNode.getConnectionState());
    }

    @Test
    @DisplayName("재연결 시도")
    void retryConnect() throws InterruptedException {
        ProtocolNode failNode = new ProtocolNode("reconnect-test", Map.of("maxRetries", 3)) {
            @Override
            public void connect() throws IOException {
                throw new IOException("서버가 죽어있음!");
            }

            @Override
            public void disconnect() {
            }

            @Override
            protected byte[] readData() throws IOException {
                return null;
            }
        };

        failNode.initialize();

    }


}
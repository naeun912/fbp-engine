package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import com.fbp.engine.message.MessageListener;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ProtocolNode extends AbstractNode {
    protected final Map<String, Object> config;
    private final long reconnectIntervalMs = 5000;
    protected List<MessageListener> listeners = new ArrayList<>();
    @Getter
    private ConnectionState connectionState;

    public ProtocolNode(String id, Map<String, Object> config) {
        super(id);
        this.config = config;
        this.connectionState = ConnectionState.DISCONNECTED;
    }

    public abstract void connect() throws IOException;

    public abstract void disconnect();

    protected abstract byte[] readData() throws IOException;

    @Override
    public void onProcess(Message message) {

    }

    @Override
    public void initialize() {
        super.initialize();
        connectionState = ConnectionState.CONNECTING;

        try {
            connect();
            this.connectionState = ConnectionState.CONNECTED;
            startReceiver();
        } catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            reconnect();
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        disconnect();
        connectionState = ConnectionState.DISCONNECTED;
        super.shutdown();
    }

    public void reconnect() {
        int maxRetries = (int) config.getOrDefault("maxRetries", 10);
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(reconnectIntervalMs);
                retryCount++;
                System.out.println("🔄재연결 시도 중... (" + retryCount + "/" + maxRetries + ")");

                connect();

                connectionState = ConnectionState.CONNECTED;
                System.out.println("✅재연결 성공!");
                break;
            } catch (Exception e) {
                connectionState = ConnectionState.ERROR;
                if (retryCount >= maxRetries) {
                    System.out.println("⛔️최대 재시도 횟수를 초과했습니다.");
                }
            }
        }
    }

    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public Object getConfig(String key) {
        return config.get(key);
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public void startReceiver() {
        Thread thread = new Thread(() -> {
            try {
                while (isConnected()) {
                    byte[] data = readData();
                    if (data != null) {
                        for (MessageListener listener : listeners) {
                            listener.onMessage("data", data);
                        }
                    }
                }
            } catch (Exception e) {
                if (connectionState != ConnectionState.DISCONNECTED) {
                    connectionState = ConnectionState.ERROR;
                    for (MessageListener listener : listeners) {
                        listener.onConnectionLost(e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();


    }

    enum ConnectionState {DISCONNECTED, CONNECTING, CONNECTED, ERROR}
}

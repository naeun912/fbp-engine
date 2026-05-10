package com.fbp.engine.node.protocolImpl;

import com.fbp.engine.message.Message;
import com.fbp.engine.message.MessageListener;
import com.fbp.engine.node.ProtocolNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class EchoProtocolNode extends ProtocolNode implements MessageListener {
    private Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    public EchoProtocolNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
        addOutputPort("out");
        this.addMessageListener(this);
    }

    @Override
    public void onProcess(Message message) {
        if (!isConnected() || socket == null) {
            return;
        }

        out.println(message.payload().toString());
    }

    @Override
    public void connect() throws IOException {
        String host = (String) getConfig("host");
        int port = (int) getConfig("port");
        this.socket = new Socket(host, port);

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    @Override
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    protected byte[] readData() throws IOException {
        if (in == null) return null;

        String line = in.readLine();
        return (line != null) ? line.getBytes() : null;
    }

    @Override
    public void onMessage(String topic, byte[] payload) {
        String response = new String(payload);
        send("out", new Message(Map.of("response", response)));
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        System.out.println("⛔️연결 끊김! 재연결을 시도합니다: " + cause.getMessage());
        reconnect();
    }
}

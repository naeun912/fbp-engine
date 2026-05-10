package com.fbp.engine.modbus;

import java.io.*;
import java.net.Socket;

public class ModbusTcpClient {
    private final int transactionId = 0;
    private final String host;
    private final int port;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(3000);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ① FC 06 요청 프레임 조립
    // ② 소켓으로 전송
    // ③ 응답 프레임 수신
    // ④ 에코백 검증 (주소, 값 일치 확인)
    // ⑤ 불일치 시 ModbusException 발생
    public void readHoldingRegisters(int unitId, int startAddress, int quantity) {

    }

}

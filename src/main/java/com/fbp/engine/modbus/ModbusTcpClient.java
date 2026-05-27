package com.fbp.engine.modbus;

import java.io.*;
import java.net.Socket;

public class ModbusTcpClient {
    private final String host;
    private final int port;
    private int transactionId = 0;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (isConnected()) {
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
        if (!isConnected()) {
            return;
        }
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void buildMbapHeader(int tid, int length, int unitId) throws IOException {
        out.writeShort(tid);
        out.writeShort(0);
        out.writeShort(length);
        out.writeByte(unitId);
    }

    private int readMbapHeader() throws IOException {
        int tid = in.readUnsignedShort();
        in.readShort();
        in.readShort();
        in.readByte();
        return tid;
    }

    public int[] readHoldingRegisters(int unitId, int startAddress, int quantity) {
        try {
            int currentTid = transactionId++;
            buildMbapHeader(currentTid, 6, unitId);
            out.writeByte(3);
            out.writeShort(startAddress);
            out.writeShort(quantity);
            out.flush();

            int resTid = readMbapHeader();
            int fnCode = in.readUnsignedByte();

            if (fnCode == 0x83) {
                throw new RuntimeException("Modbus Error: FC 03");
            }

            int byteCount = in.readUnsignedByte();
            int[] registers = new int[quantity];
            for (int i = 0; i < quantity; i++) {
                registers[i] = in.readUnsignedShort();
            }
            return registers;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSingleRegister(int unitId, int address, int value) {
        try {
            int currentTid = transactionId++;
            buildMbapHeader(currentTid, 6, unitId);
            out.writeByte(6);
            out.writeShort(address);
            out.writeShort(value);
            out.flush();

            int resTid = readMbapHeader();
            int fnCode = in.readUnsignedByte();

            if (fnCode == 0x86) {
                throw new RuntimeException("Modbus Error: FC 06");
            }

            int resAddress = in.readUnsignedShort();
            int resValue = in.readUnsignedShort();

            if (resTid != currentTid || resAddress != address || resValue != value) {
                throw new RuntimeException("에코백 검증 실패!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

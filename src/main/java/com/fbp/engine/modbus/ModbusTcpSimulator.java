package com.fbp.engine.modbus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ModbusTcpSimulator {
    private final int[] registers;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public ModbusTcpSimulator(int port, int registerCount) {
        this.port = port;
        this.registers = new int[registerCount];
    }

    public void start() {
        running = true;
        Thread thread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    // 다중 클라이언트를 렉 없이 사용하는 방법 이중으로 Thread 사용하기(?)
                    Thread thread1 = new Thread(() -> handleClient(clientSocket));
                    thread1.setDaemon(true);
                    thread1.start();
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트 요청 처리 (핵심 로직)
    private void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            while (running) {
                // 필요한 값들을 빼오기 위해 통로비우기용 변수도 선언해서 순서대로 변수에 값 담기
                int tid = in.readUnsignedShort();
                int pid = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int unitId = in.readUnsignedByte();

                int fnCode = in.readUnsignedByte();

                if (fnCode == 3) {
                    int address = in.readUnsignedShort();
                    int quantity = in.readUnsignedShort();

                    // 자기가 가진 메모리 용량을 초과하는지 체크, 벗어나면
                    if (address < 0 || address + quantity > registers.length) {
                        sendError(out, tid, unitId, 0x03, 0x02);
                        continue;
                    }

                    /*
                    3 + (quantity * 2)의 정체: MBAP 헤더의 Length 자리에 들어갈 값입니다.
                    Unit ID(1바이트) + Function Code(1바이트) + Byte Count(1바이트) = 3바이트에다가,
                    실제 데이터 크기(레지스터 1개당 2바이트씩)를 더해준 총 길이를 장비에게 알려주는 규격
                     */
                    out.writeShort(tid);
                    out.writeShort(0);
                    out.writeShort(3 + (quantity * 2));
                    out.writeByte(unitId);
                    out.writeByte(3);
                    out.writeByte(quantity * 2);
                    for (int i = 0; i < quantity; i++) {
                        out.writeShort(registers[address + i]);
                    }

                    /*
                    클라이언트가 "여기에 이 값 좀 써줘!" 하면 시뮬레이터 내부의 registers[] 배열에 그 값을 즉시 기록합니다.
                    그리고 Modbus 표준 규격에 따라 "네가 요청한 주소에 이 값 잘 썼어!" 하고 안심시켜 주기 위해
                     클라이언트가 보낸 데이터 주소와 값을 그대로 복사해서 다시 돌려주는(에코백) 친절한 로직.
                     */
                } else if (fnCode == 6) {
                    int address = in.readUnsignedShort();
                    int value = in.readUnsignedShort();

                    if (address < 0 || address >= registers.length) {
                        sendError(out, tid, unitId, 0x06, 0x02);
                        continue;
                    }

                    registers[address] = value;

                    out.writeShort(tid);
                    out.writeShort(0);
                    out.writeShort(6);
                    out.writeByte(unitId);
                    out.writeByte(6);
                    out.writeShort(address);
                    out.writeShort(value);
                }
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("[Simulator] 클라이언트 연결이 종료되었습니다: " + socket.getInetAddress());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    //  ⑤ 잘못된 주소면 에러 응답(Exception Code 0x02) 전송을 위해 따로 구현한 메서드
    private void sendError(DataOutputStream out, int tid, int unitId, int fnCode, int exceptionCode) throws IOException {
        out.writeShort(tid);
        out.writeShort(0);
        out.writeShort(3);
        out.writeByte(unitId);
        out.writeByte(fnCode + 0x80);
        out.writeByte(exceptionCode);
        out.flush();
    }

    public void setRegister(int address, int value) {
        registers[address] = value;
    }

    public int getRegister(int address) {
        return registers[address];
    }
}
package com.fbp.engine.runner;

import com.fbp.engine.modbus.ModbusTcpClient;
import com.fbp.engine.modbus.ModbusTcpSimulator;

import java.util.Arrays;

/**
 * Hello world!
 */
public class ModbusTcp {

    public static void main(String[] args) {
        int port = 5020;
        int registerCount = 10;

        System.out.println("=== 1. Modbus TCP 시뮬레이터 시작 ===");
        // 1. 명세서 조건대로 포트 5020, 레지스터 10개짜리 보관함 생성
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(port, registerCount);

        // 2. 명세서가 요구한 초기값 세팅 [250, 600, 1, 0, 0, ...]
        simulator.setRegister(0, 250);
        simulator.setRegister(1, 600);
        simulator.setRegister(2, 1);

        // 시뮬레이터 서버 가동! (클라이언트 접속을 기다리는 상태가 됨)
        simulator.start();
        System.out.println("시뮬레이터가 포트 " + port + "에서 클라이언트를 기다립니다.");


        System.out.println("\n=== 2. Modbus TCP 클라이언트 연결 ===");
        // 3. 내 컴퓨터(localhost)의 5020 포트에 떠 있는 시뮬레이터 주소로 클라이언트 생성
        ModbusTcpClient client = new ModbusTcpClient("localhost", port);
        client.connect(); // 실제 소켓 연결 수립
        System.out.println("시뮬레이터에 연결되었습니다.");


        try {
            System.out.println("\n=== 3. 주소 0~2 레지스터 읽기 (FC 03) ===");
            // unitId=1, startAddress=0, quantity=3 (0번 주소부터 3개 읽기)
            int[] initialValues = client.readHoldingRegisters(1, 0, 3);
            System.out.println("처음 읽은 값 (예상: [250, 600, 1]): " + Arrays.toString(initialValues));


            System.out.println("\n=== 4. 주소 2에 값 100 쓰기 (FC 06) ===");
            // unitId=1, address=2, value=100 (2번 사물함에 100 쓰기)
            client.writeSingleRegister(1, 2, 100);
            System.out.println("주소 2번에 100 쓰기 요청 완료.");


            System.out.println("\n=== 5. 변경 확인을 위해 다시 주소 0~2 읽기 (FC 03) ===");
            int[] updatedValues = client.readHoldingRegisters(1, 0, 3);
            System.out.println("바뀐 후 읽은 값 (예상: [250, 600, 100]): " + Arrays.toString(updatedValues));

        } catch (Exception e) {
            System.err.println("테스트 도중 에러 발생!");
            e.printStackTrace();
        } finally {
            // 통신 다 끝났으니 소켓 정리하기
            System.out.println("\n=== 6. 테스트 종료 및 자원 정리 ===");
            client.disconnect();
            simulator.stop();
            System.out.println("클라이언트와 시뮬레이터가 안전하게 닫혔습니다.");
        }
    }
}

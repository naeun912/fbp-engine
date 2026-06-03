package com.fbp.engine.modbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class ModbusTcpClientTest {

    private ModbusTcpClient client;
    private DataOutputStream mockOut;
    private DataInputStream mockIn;

    @BeforeEach
    void setUp() throws Exception {
        client = new ModbusTcpClient("localhost", 5020);

        mockOut = mock(DataOutputStream.class);
        mockIn = mock(DataInputStream.class);

        setPrivateField(client, "out", mockOut);
        setPrivateField(client, "in", mockIn);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    @DisplayName("FC 03 요청 프레임 조립")
    void testFC03RequestFrame() throws Exception {
        // 서버 응답 Mocking (TID 0, PID 0, Length 3, UID 1, FC 3, ByteCount 0)
        when(mockIn.readUnsignedShort()).thenReturn(0); // TID
        when(mockIn.readShort()).thenReturn((short) 0, (short) 3); // PID, Length
        when(mockIn.readByte()).thenReturn((byte) 1); // UnitID
        when(mockIn.readUnsignedByte()).thenReturn(3, 0); // FC 3, ByteCount 0

        client.readHoldingRegisters(1, 100, 2);

        // 아예 순서고 뭐고 횟수까지 딱 정해서 검증해버립시다!
        verify(mockOut, times(2)).writeShort(0); // TID(0)와 PID(0)가 2번 호출됨!
        verify(mockOut).writeShort(6);           // Length
        verify(mockOut).writeByte(1);            // UnitID
        verify(mockOut).writeByte(3);            // FC
        verify(mockOut).writeShort(100);         // Addr
        verify(mockOut).writeShort(2);           // Qty
    }

    @Test
    @DisplayName("FC 06 요청 프레임 조립")
    void testFC06RequestFrame() throws Exception {
        // 서버 응답 Mocking (TID 0, PID 0, Length 6, UID 1, FC 6, Addr 2, Val 500)
        when(mockIn.readUnsignedShort()).thenReturn(0, 2, 500); // TID, ResAddr, ResVal
        when(mockIn.readShort()).thenReturn((short) 0, (short) 6); // PID, Length
        when(mockIn.readByte()).thenReturn((byte) 1); // UnitID
        when(mockIn.readUnsignedByte()).thenReturn(6); // FC 6

        client.writeSingleRegister(1, 2, 500);

        verify(mockOut).writeByte(6); // FC 06
        verify(mockOut).writeShort(2); // Address
        verify(mockOut).writeShort(500); // Value
    }

    @Test
    @DisplayName("MBAP 헤더 구조")
    void testMbapHeaderStructure() throws Exception {
        when(mockIn.readUnsignedShort()).thenReturn(123);
        when(mockIn.readShort()).thenReturn((short) 0, (short) 3);
        when(mockIn.readByte()).thenReturn((byte) 1);
        when(mockIn.readUnsignedByte()).thenReturn(3, 0);

        setPrivateField(client, "transactionId", 123);

        client.readHoldingRegisters(1, 999, 1);

        // MBAP 헤더 검증
        verify(mockOut).writeShort(123); // TID (123)
        verify(mockOut).writeShort(0);   // PID (0)
        verify(mockOut).writeShort(6);   // Length
        verify(mockOut).writeByte(1);    // UnitID

        // 주소 검증 (999를 썼는지 확인)
        verify(mockOut).writeShort(999);
    }

    @Test
    @DisplayName("Transaction ID 증가")
    void testTransactionIdIncrement() throws Exception {
        int initialId = (int) getPrivateField(client, "transactionId");

        // [TID, Req1Data, TID, ResAddr, ResVal] 순서로 응답 흉
        when(mockIn.readUnsignedShort()).thenReturn(0, 1234, 1, 0, 0);

        // [PID, Len] 쌍으로 두 번 응답 (Req1: FC3용, Req2: FC6용)
        when(mockIn.readShort()).thenReturn((short) 0, (short) 3, (short) 0, (short) 6);

        // UnitID 응답
        when(mockIn.readByte()).thenReturn((byte) 1, (byte) 1);

        // [FC3, ByteCount, FC6] 순서로 응답
        when(mockIn.readUnsignedByte()).thenReturn(3, 2, 6);

        client.readHoldingRegisters(1, 0, 1);
        client.writeSingleRegister(1, 0, 0);

        assertEquals(initialId + 2, (int) getPrivateField(client, "transactionId"));
    }

    @Test
    @DisplayName("초기 상태")
    void testInitialState() {
        assertFalse(client.isConnected());
    }
}
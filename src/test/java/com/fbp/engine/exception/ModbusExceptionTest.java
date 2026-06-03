package com.fbp.engine.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModbusExceptionTest {
    @Test
    @DisplayName("에러 메세지 및 코드 검증")
    void testModbusException() {
        ModbusException ex = new ModbusException(0x03, ModbusException.ILLEGAL_DATA_ADDRESS);

        String expected = "MODBUS 에러 - FC: 0x03, Exception: 0x02 (Illegal Data Address)";
        assertEquals(expected, ex.getMessage());
        assertEquals(0x02, ex.getExceptionCode());
    }

    @Test
    @DisplayName("Unknown Error 처리 검증")
    void testUnknownErrorMessage() {
        ModbusException ex = new ModbusException(0x06, 0x99);

        assertTrue(ex.getMessage().contains("Unknown Error"));
    }
}
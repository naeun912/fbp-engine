package com.fbp.engine.exception;

import lombok.Getter;

public class ModbusException extends Exception {
    public static final int ILLEGAL_FUNCTION = 0x01;
    public static final int ILLEGAL_DATA_ADDRESS = 0x02;
    public static final int ILLEGAL_DATA_VALUE = 0x03;
    public static final int SLAVE_DEVICE_FAILURE = 0x04;

    private final int functionCode;
    @Getter
    private final int exceptionCode;

    public ModbusException(String message) {
        super(message);
        this.functionCode = 0;
        this.exceptionCode = 0;
    }

    public ModbusException(int functionCode, int exceptionCode) {
        this.functionCode = functionCode;
        this.exceptionCode = exceptionCode;
    }

    @Override
    public String getMessage() { //
        String desc = switch (exceptionCode) {
            case ILLEGAL_FUNCTION -> "Illegal Function";
            case ILLEGAL_DATA_ADDRESS -> "Illegal Data Address";
            case ILLEGAL_DATA_VALUE -> "Illegal Data Value";
            case SLAVE_DEVICE_FAILURE -> "Slave Device Failure";
            default -> "Unknown Error";
        };
        return String.format("MODBUS 에러 - FC: 0x%02X, Exception: 0x%02X (%s)",
                functionCode, exceptionCode, desc);
    }

}
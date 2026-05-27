package com.fbp.engine.modbus;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.ProtocolNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModbusWriterNode extends ProtocolNode {
    private ModbusTcpClient client;

    private String host;
    private int port;
    private int slaveId;
    private int registerAddress;
    private String valueField;
    private double scale;

    public ModbusWriterNode(String id, Map<String, Object> config) {
        super(id, config);
        addOutputPort("result");
        addInputPort("in");
        this.host = (String) config.get("host");
        this.port = config.containsKey("port") ? (int) config.get("port") : 502;
        this.slaveId = (int) config.get("slaveId");
        this.registerAddress = (int) config.get("registerAddress");
        this.valueField = (String) config.get("valueField");
        this.scale = config.containsKey("scale") ? (double) config.get("scale") : 1.0;
    }

    @Override
    public void connect() throws IOException {
        if (client == null) {
            client = new ModbusTcpClient(host, port);
        }
        client.connect();
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onProcess(Message message) {

        try {
            Object rawValue = message.get(valueField);

            if (rawValue == null) {
                System.err.println("[WriterNode] 페이로드에 '" + valueField + "' 키가 존재하지 않습니다.");
                return;
            }
            // 정수형일수도 있고 실수형일 수도 있어서 number로 유연하게 받기.
            double parsedValue = ((Number) rawValue).doubleValue();

            int finalValue = (int) (parsedValue * scale);

            client.writeSingleRegister(slaveId, registerAddress, finalValue);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("status", "success");
            resultMap.put("writtenValue", finalValue);

            Message resultMessage = message.withPayload(resultMap);
            send("result", resultMessage);

        } catch (Exception e) {
            System.err.println("[WriterNode] Modbus 쓰기 작업 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    protected byte[] readData() throws IOException {
        return new byte[0];
    }
}

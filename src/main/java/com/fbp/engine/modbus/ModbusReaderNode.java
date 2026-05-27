package com.fbp.engine.modbus;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.ProtocolNode;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ModbusReaderNode extends ProtocolNode {
    private final String host;
    private final int port;
    private final int slaveId;
    private final int startAddress;
    private final int count;
    private final Map<String, Object> registerMapping;
    private ModbusTcpClient client;

    public ModbusReaderNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("trigger");
        addOutputPort("out");
        addOutputPort("error");
        this.host = (String) config.get("host");
        this.port = config.containsKey("port") ? (int) config.get("port") : 502;
        this.slaveId = (int) config.get("slaveId");
        this.startAddress = (int) config.get("startAddress");
        this.count = (int) config.get("count");
        this.registerMapping = (Map<String, Object>) config.get("registerMapping");
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
        super.onProcess(message);

        try {
            int[] values = client.readHoldingRegisters(slaveId, startAddress, count);

            Map<String, Object> resultMap = new HashMap<>();

            if (registerMapping != null && !registerMapping.isEmpty()) {
                for (Map.Entry<String, Object> entry : registerMapping.entrySet()) {
                    String key = entry.getKey();
                    int index = (int) entry.getValue();

                    if (index >= 0 && index < values.length) {
                        resultMap.put(key, values[index]);
                    }
                }
            } else {
                resultMap.put("values", values);
            }
            Message newMessage = message.withPayload(resultMap);

            send("out", newMessage);

        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());

            Message errorMessage = new Message(errorMap);
            send("error", errorMessage);
        }
    }

    @Override
    protected byte[] readData() throws IOException {
        return new byte[0];
    }
}

package com.fbp.engine.node.impl;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class AlertNode extends AbstractNode {
    public AlertNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
//        Object temp = message.get("temperature");
//        Object humid = message.get("humidity");
//        Object sensorId = message.get("sensorId");
//        if (temp == null || sensorId == null || humid == null) {
//            System.out.println("[⛔️경고] 알 수 없는 센서 데이터!!\n");
//        }
//
//        System.out.printf("[⚠️경고] 센서 '%s' 온도 %s°C — 임계값 초과!\n", sensorId, temp);
//        System.out.printf("[⚠️경고] 센서 '%s' 습도 %s%% — 임계값 초과!\n", sensorId, humid);

        Object sensorId = message.get("sensorId");
        String fieldName = message.get("checkField"); // 필터가 적어준 필드명
        Object value = message.get(fieldName); // 그 필드명으로 값을 꺼냄
        Object unit = message.get("unit");

        if (sensorId == null || fieldName == null || value == null) {
            System.out.println("[⛔️경고] 데이터 누락!");
            return;
        }

        // 이제 온도든 습도든 미세먼지든 다 소화 가능!
        System.out.printf("[⚠️경고] 센서 '%s' %s: %s%s — 임계값 초과!\n",
                sensorId, fieldName, value, unit);
    }
}

package com.fbp.engine.message;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;



class MessageTest {
    private Message message;

    @BeforeEach
    void setUp(){
        Map<String, Object> payload = Map.of("temperature", 25.5);
        message = new Message(payload);
    }
    @Test
    @DisplayName("생성시 ID 자동 할당")
    void idTest(){
        assertNotNull(message.id());
        assertFalse(message.id().isBlank());

        System.out.println("생성된 ID: " + message.id());
    }

    @Test
    @DisplayName("생성 시 timestamp 자동 기록")
    void timestampTest(){
        assertTrue(message.timestamp()>0);
        System.out.println("time stamp : " +message.timestamp());
    }

    @Test
    @DisplayName("페이로드 조회")
    void payloadCheck(){
        Double temp = message.get("temperature");

        assertEquals(25.5, temp);

        System.out.println("조회 성공! 온도: " + temp);
    }

    @Test
    @DisplayName("제네릭 get 타입 캐스팅")
    void getTest(){
        Double temp = message.get("temperature");
        assertEquals(25.5, temp);
    }

    @Test
    @DisplayName("존재하지 않는 키 조회")
    void nullKeyTest(){
        String key = message.get("흠냐");

        assertNull(key);

        System.out.println("키 :" + key);
    }

    @Test
    @DisplayName("페이로드 불변 - 외부수정 차단")
    void payloadPutTest(){
        Map<String, Object> payloadMsg = message.payload();

        assertThrows(UnsupportedOperationException.class, () -> {
            payloadMsg.put("key", "value");
        });

        System.out.println("UnsupportedOperationException 발생");
    }

    @Test
    @DisplayName("페이로드 불변 - 원본 Map 수정 무영향")
    void payloadMapTest(){
        java.util.Map<String, Object> payload1 = new java.util.HashMap<>();
        payload1.put("temperature", 25.5);

        Message testMessage = new Message(payload1);

        payload1.put("temperature", 100.0);
        payload1.put("new", "keyyyy");

        assertEquals(25.5, (Double) testMessage.get("temperature"));
        assertNull(testMessage.get("new"));

        System.out.println("원본 메세지는 무사한가? : " + testMessage.payload());
    }

    @Test
    @DisplayName("withEntry - 새 객체 반환")
    void withEntryNewTest(){
        Message newMessage = message.withEntry("status", "OK");
        assertNotSame(message, newMessage);

        System.out.println("원본 : " + message.payload());
        System.out.println("새로 추가한 거 : " + newMessage.payload());
    }

    @Test
    @DisplayName("withEntry - 원본 불변")
    void withEntryTest(){
        Message newMessage = message.withEntry("status", "ok");

        assertNull(message.get("status"));

        assertEquals(1, message.payload().size());

        System.out.println("원본 : " + message.payload());
        System.out.println("새로 추가한 거 : " + newMessage.payload());
    }

    @Test
    @DisplayName("withEntry - 새 메세지에 값 존재")
    void withEntryNewHaveTest(){
        Message newMessage = message.withEntry("status", "ok");

        assertEquals("ok", newMessage.get("status"));

        System.out.println("새 메세지 : " + newMessage.payload());
    }

    @Test
    @DisplayName("hasKey - 존재하는 키")
    void withEntryExistenceTest(){
        assertTrue(message.hasKey("temperature"));
    }

    @Test
    @DisplayName("hasKey - 없는 키")
    void withEntryNoKeyTest(){
        assertFalse(message.hasKey("없는 키"));
    }

    @Test
    @DisplayName("withoutKey - 키 제거 확인")
    void withoutKeyTest(){
        Message newMessage = message.withEntry("status", "ok");

        Message remove = newMessage.withoutKey("status");

        assertNull(remove.get("status"));
        assertEquals(25.5, (Double) remove.get("temperature"));
    }

    @Test
    @DisplayName("withoutKey - 원본 불변")
    void withoutKeyHAHATest(){

        message.withoutKey("temperature");

        assertNotNull(message.get("temperature"));
        assertEquals(1, message.payload().size());
    }

    @Test
    @DisplayName("toString 포맷")
    void toStringTest(){
        assertTrue(message.toString().contains(message.payload().toString()));
    }
}
package com.fbp.engine.node.rule;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleExpressionTest {

    @Test
    @DisplayName("파싱 — 숫자 비교")
    void testNumberComparison() {
        RuleExpression expr = RuleExpression.parse("temperature > 30.0");
        Message msg = new Message(Map.of("temperature", 35.0));

        assertTrue(expr.evaluate(msg));
    }

    @Test
    @DisplayName("파싱 — 문자열 비교")
    void testStringComparison() {
        assertDoesNotThrow(() -> {
            RuleExpression expr = RuleExpression.parse("status == ON");
            Message msg = new Message(Map.of("status", "ON"));
            assertTrue(expr.evaluate(msg));
        });
    }

    @Test
    @DisplayName("모든 연산자")
    void testAllOperators() {
        // >, >=, <, <=, ==, != 각각에 대해 테스트
        assertTrue(RuleExpression.parse("val > 10").evaluate(new Message(Map.of("val", 11))));
        assertTrue(RuleExpression.parse("val >= 10").evaluate(new Message(Map.of("val", 10))));
        assertTrue(RuleExpression.parse("val < 10").evaluate(new Message(Map.of("val", 9))));
        assertTrue(RuleExpression.parse("val <= 10").evaluate(new Message(Map.of("val", 10))));
        assertTrue(RuleExpression.parse("val == 10").evaluate(new Message(Map.of("val", 10.0))));
        assertTrue(RuleExpression.parse("val != 10").evaluate(new Message(Map.of("val", 11))));
    }

    @Test
    @DisplayName("잘못된 표현식")
    void testMalformedExpression() {
        assertThrows(Exception.class, () -> {
            RuleExpression.parse("temperature>30.0");
        });
    }

    @Test
    @DisplayName("필드 없음")
    void testMissingField() {
        RuleExpression expr = RuleExpression.parse("temperature > 30.0");
        Message msg = new Message(Map.of("humidity", 80.0));

        assertDoesNotThrow(() -> {
            try {
                expr.evaluate(msg);
            } catch (NullPointerException e) {
                fail("필드가 없을 때 NullPointerException이 발생함");
            }
        });
    }
}
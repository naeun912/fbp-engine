package com.fbp.engine.node.rule;

import com.fbp.engine.message.Message;

public class RuleExpression {

    private String field;
    private String operator;
    private Object value;

    public static RuleExpression parse(String expression) {
        String[] parts = expression.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid expression");
        }

        RuleExpression expr = new RuleExpression();
        expr.field = parts[0];
        expr.operator = parts[1];

        try {
            expr.value = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            expr.value = parts[2].replace("'", "");
        }
        return expr;
    }

    public boolean evaluate(Message message) {
        Object msgValue = message.get(field);

        if (msgValue == null) return false;

        if (msgValue instanceof Number && value instanceof Number) {
            double mVal = ((Number) msgValue).doubleValue();
            double vVal = ((Number) value).doubleValue();
            return switch (operator) {
                case ">" -> mVal > vVal;
                case ">=" -> mVal >= vVal;
                case "<" -> mVal < vVal;
                case "<=" -> mVal <= vVal;
                case "==" -> mVal == vVal;
                case "!=" -> mVal != vVal;
                default -> false;
            };
        } else {
            String mStr = msgValue.toString();
            String vStr = value.toString();
            return switch (operator) {
                case "==" -> mStr.equals(vStr);
                case "!=" -> !mStr.equals(vStr);
                default -> false;
            };
        }
    }
}




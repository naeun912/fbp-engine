package com.fbp.engine.node.rule;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CompositeRuleNode extends AbstractNode {
    private List<Predicate> conditions;
    private Operator operator;

    public CompositeRuleNode(String id, Operator operator) {
        super(id);
        this.conditions = new ArrayList<>();
        this.operator = operator;
        addOutputPort("match");
        addOutputPort("mismatch");
        addInputPort("in");
    }

    public void addCondition(Predicate<Message> condition) {
        this.conditions.add(condition);
    }

    // 문자열 기반 조건 (내부에서 RuleExpression 로직 활용)
    public void addCondition(String field, String op, Object value) {
        // 이 람다식이 내부적으로 RuleExpression 역할을 수행하게 됩니다.
        this.addCondition((message) -> {
            Object msgValue = message.get(field);
            if (msgValue == null) return false;

            // 숫자 비교인 경우 (Double 등으로 변환하여 처리)
            if (msgValue instanceof Number && value instanceof Number) {
                double v1 = ((Number) msgValue).doubleValue();
                double v2 = ((Number) value).doubleValue();

                switch (op) {
                    case ">":
                        return v1 > v2;
                    case ">=":
                        return v1 >= v2;
                    case "<":
                        return v1 < v2;
                    case "<=":
                        return v1 <= v2;
                    case "==":
                        return v1 == v2;
                    case "!=":
                        return v1 != v2;
                }
            }

            switch (op) {
                case "==":
                    return msgValue.equals(value);
                case "!=":
                    return !msgValue.equals(value);
            }

            return false;
        });
    }


    @Override
    public void onProcess(Message message) {
        boolean finalResult;

        if (operator == Operator.AND) {
            finalResult = conditions.stream().allMatch(cond -> cond.test(message));
        } else {
            finalResult = conditions.stream().anyMatch(cond -> cond.test(message));
        }

        if (finalResult) {
            send("match", message);
        } else {
            send("mismatch", message);
        }
    }
}

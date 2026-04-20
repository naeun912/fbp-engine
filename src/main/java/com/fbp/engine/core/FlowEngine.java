package com.fbp.engine.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FlowEngine {
    private final Map<String, Flow> flows;
    private State state;

    public FlowEngine() {
        this.flows = new HashMap<>();
        this.state = State.INITIALIZED;
    }

    public void register(Flow flow) {
        flows.put(flow.getId(), flow);
        System.out.printf("[Engine] 플로우 '%s' 등록됨\n", flow.getId());
    }

    public void startFlow(String flowId) {
        Flow flow = flows.get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("Flow가 존재하지 않습니다. ID : [" + flowId + "]\n");
        }

        List<String> errors = flow.validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException(errors.size() + "개의 validate 상태 에러가 존재 : " + errors + "\n");
        }

        flow.initialize();
        this.state = State.RUNNING;
        flow.setState(Flow.State.RUNNING);
        System.out.printf("[Engine] 플로우 '%s' 시작됨\n", flow.getId());

    }

    public void stopFlow(String flowId) {
        Flow flow = flows.get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("Flow가 존재하지 않습니다. ID : [" + flowId + "]\n");
        }
        flow.shutdown();
        flow.setState(Flow.State.STOPPED);
        System.out.printf("[Engine] 플로우 '%s' 정지됨\n", flow.getId());
    }

    public void shutdown() {
        for (Flow flow : flows.values()) {
            flow.shutdown();
        }
        this.state = State.STOPPED;
    }

    public Map<String, Flow> getFlows() {
        return flows;
    }

    public State getState() {
        return state;
    }

    public void listFlows() {
        System.out.println("등록된 플로우 목록:");
        for (Flow flow : flows.values()) {
            System.out.printf("[%s] %s%n", flow.getId(), flow.getState());
        }
    }

    public void startCLI() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Engine] CLI 시작 (exit < 종료 명령어)");

        while (true) {
            System.out.println("fbp> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("exit")) {
                this.shutdown();
                break;
            }

            processCommand(line);
        }
    }

    public void processCommand(String command) {
        try {
            String[] parts = command.split(" ", 2);
            String cmd = parts[0];

            switch (cmd) {
                case "list":
                    listFlows();
                    break;

                case "start":
                    if (parts.length < 2) throw new IllegalStateException("명령어 뒤에 ID 누락");
                    startFlow(parts[1]);
                    break;
                case "stop":
                    if (parts.length < 2) throw new IllegalStateException("명령어 뒤에 ID 누락");
                    stopFlow(parts[1]);
                    break;

                default:
                    System.out.println("등록되지 않은 명령어 입니다 : " + cmd);
            }
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    public enum State {
        INITIALIZED,
        RUNNING,
        STOPPED
    }
}

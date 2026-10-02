# ⚡ FBP Engine (Flow-Based Programming Engine)

> **Java 기반 흐름 기반 프로그래밍(Flow-Based Programming) 엔진 & 데이터 처리 시스템**

---

## 📌 프로젝트 소개
FBP(Flow-Based Programming) 개념을 도입하여 노드(Node) 간 데이터 흐름을 정의하고, 메시지 기반 이벤트 처리 및 통신 프로토콜을 수행하는 엔진 시스템입니다.

---

## 🛠️ 주요 기능 & 특징
- **Node & Flow Architecture**: InputPort, OutputPort, FlowEngine을 통한 노드 연결 및 파이프라인 제어
- **메시지 통신 프로토콜**: MQTT Publisher/Subscriber 및 Modbus TCP 프로토콜 기반 데이터 수집/전송
- **규칙 처리 엔진 (Rule Engine)**: CompositeRuleNode를 이용한 실시간 노드 상태 조건 검증
- **테스트 자동화**: JUnit5 기반 단위 테스트 및 크로스 프로토콜 시나리오 검증

---

## 💻 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot, Maven
- **Protocol & Messaging**: MQTT (Eclipse Paho), Modbus TCP
- **Testing**: JUnit 5, Mockito

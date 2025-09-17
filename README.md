# OpenOrder
## kafka 이벤트 기반 msa 주문 처리 시스템

# 🚀 MSA-Kafka-Lab

MSA 아키텍처 + Kafka 이벤트 기반 통신 학습을 위한 실습 프로젝트입니다.  
Redpanda(카프카 호환 브로커)와 Spring Boot 마이크로서비스를 통해  
주문 → 재고 확인 → 알림 전송의 이벤트 흐름을 구현합니다.  

---

## 📑 프로젝트 개요

### 목표
- MSA 아키텍처 기본 구조 이해
- 서비스 간 **비동기 이벤트 기반 통신** 구현
- Kafka Producer/Consumer 사용법 실습
- 장애 상황 처리 및 Dead Letter Queue(DLQ) 경험

### 시나리오
1. **Order-Service**  
   - 사용자 주문 API → `order.created` 이벤트 발행  
2. **Inventory-Service**  
   - `order.created` 소비 → 재고 확인  
   - 성공: `inventory.reserved`, 실패: `inventory.rejected` 발행  
   - 장애 상황: 재시도 후 실패 시 `order.created.DLT`로 이동  
3. **Notification-Service**  
   - `inventory.*` 이벤트 구독 → 알림(로그 출력)

---

## 🏗️ 아키텍처 구조

```
[ User ]
   |
   v
[Order Service] -- produces --> (Kafka: order.created)
                                 |
                                 v
                      [Inventory Service]
                         |          |
                         v          v
            (Kafka: inventory.reserved / inventory.rejected)
                                 |
                                 v
                      [Notification Service]

* 장애 시:
   order.created -> 재시도 실패 -> order.created.DLT
```

---

## ⚙️ 기술 스택

- **Java 17**, **Spring Boot 3.3.x**
- **Spring Kafka**
- **Docker Compose**
  - Redpanda (Kafka 호환 브로커)
  - Kafka UI (토픽/메시지 모니터링)

---

## 📂 프로젝트 구조

```
msa-kafka-lab/
 ├─ docker-compose.yml        # Redpanda + Kafka UI
 ├─ order-service/            # 주문 서비스 (producer)
 ├─ inventory-service/        # 재고 서비스 (consumer+producer, DLQ)
 └─ notification-service/     # 알림 서비스 (consumer)
```

---

## ▶️ 실행 방법

### 1. 카프카 환경 실행
```bash
docker compose up -d
```
- Kafka UI: [http://localhost:8081](http://localhost:8081) 접속 가능해야 합니다.  

### 2. 서비스 실행
각 서비스 디렉토리(`order-service`, `inventory-service`, `notification-service`)에서 실행:
```bash
./gradlew bootRun
```
- Order-Service → 8080  
- Inventory-Service → 8082  
- Notification-Service → 8083  

### 3. 주문 API 호출
```bash
curl -X POST http://localhost:8080/api/orders   -H "Content-Type: application/json"   -d '{"userId":42,"items":[{"sku":"ABC-001","qty":2}]}'
```

응답 예시:
```json
{"orderId":12345,"status":"PENDING"}
```

### 4. 메시지 확인
- Kafka UI에서 토픽 메시지 확인  
- `inventory-service`, `notification-service` 로그 확인  

---

## 🧪 장애 주입 & DLQ 확인

테스트 실패 주문:
```bash
curl -X POST http://localhost:8080/api/orders   -H "Content-Type: application/json"   -d '{"userId":42,"items":[{"sku":"FAIL","qty":1}]}'
```

- `inventory-service`에서 재시도 후 실패 발생  
- Kafka UI에서 `order.created.DLT` 토픽 생성/메시지 확인  

---

## 📌 학습 포인트

- 서비스 간 **동기 REST 호출 대신 비동기 이벤트 기반 통신** 구조 경험  
- **Kafka Producer/Consumer** 기초 학습  
- **재시도 + DLQ 처리**로 안정성 확보  
- Kafka UI를 통한 토픽/메시지 모니터링  

---

## 📈 향후 확장 아이디어

- Outbox 패턴 + DB 트랜잭션 연동  
- Avro + Schema Registry 적용  
- OpenTelemetry 기반 분산 트레이싱  
- Notification 실제 구현 (이메일/SMS/푸시)

---

## 📝 License
This project is licensed under the MIT License.

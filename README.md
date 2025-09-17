# 📑 OpenOrder
## kafka 이벤트 기반 msa 주문 처리 시스템


**MSA** 아키텍처 + **Kafka 이벤트** 기반 통신 실습 프로젝트
Redpanda(카프카 호환 브로커)와 Spring Boot msa를 통한
주문 → 재고 확인 → 알림 전송의 이벤트 흐름  

---

### 주요 기능
- **비동기 이벤트 기반 통신**
- Kafka Producer/Consumer 구조로 이벤트 처리
- Dead Letter Queue **(DLQ)** 로 장애 주문 처리

### MSA 아키텍처
1. **Order-Service**  
   - 사용자 주문 **POST /api/orders** → `order.created` 이벤트 발행  
2. **Inventory-Service**  
   - `order.created` 소비 → 재고 확인  
   - 성공: `inventory.reserved`, 실패: `inventory.rejected` 발행  
   - 장애 상황: 재시도 후 실패 시 Topic `order.created.DLT`    
3. **Notification-Service**  
   - `inventory.*` 이벤트 구독 → 알림

---

##  기술 스택

![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-MSA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Kafka-Event--Driven-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Redpanda](https://img.shields.io/badge/Redpanda-Streaming-FF4438?style=for-the-badge&logo=redpanda&logoColor=white)

---

## 프로젝트 구조

```
msa-kafka-lab/
 ├─ docker-compose.yml        # Redpanda + Kafka UI
 ├─ order-service/            # 주문 서비스 (producer)
 ├─ inventory-service/        # 재고 서비스 (consumer+producer, DLQ)
 └─ notification-service/     # 알림 서비스 (consumer)
```

---

## ▶ 실행 방법

### 1. 카프카 환경 실행
```cmd
docker compose up -d
```
- Kafka UI: [http://localhost:8081](http://localhost:8081) 

### 2. 서비스 실행
각 서비스 디렉토리(`order-service`, `inventory-service`, `notification-service`)에서 실행:
```bash
./gradlew bootRun
```
- KafkaOrder → 8080  
- KafkaInventory → 8082  
- KafkaNotification → 8083  

### 3. 주문 API
```
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

##  장애 주입 & DLQ 확인

테스트 실패 주문: **sku에 Fail 넣음**
```
curl -X POST http://localhost:8080/api/orders   -H "Content-Type: application/json"   -d '{"userId":42,"items":[{"sku":"FAIL","qty":1}]}'
```

- `inventory-service`에서 재시도 후 실패 발생  
- Kafka UI에서 `order.created.DLT` 토픽 생성/메시지 확인  

---


## 📈 향후 확장 아이디어

- Outbox 패턴 + DB 트랜잭션 연동  
- Notification 실제 구현 (이메일/SMS/푸시)

---


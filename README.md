# OpenOrder
## kafka 이벤트 기반 msa 주문 처리 시스템

MSA 아키텍처 + Kafka 이벤트 기반 통신 학습을 위한 실습 프로젝트

목적

마이크로서비스 아키텍처(MSA) 기본 구조 경험

서비스 간 이벤트 기반 비동기 통신 구현

Kafka를 통한 메시징 프로듀서/컨슈머 실습

장애 상황 처리 및 Dead Letter Queue(DLQ) 활용

시나리오

Order-Service

사용자 주문을 API로 받아 order.created 이벤트 발행

Inventory-Service

주문 이벤트를 소비 → 재고 확인 후 inventory.reserved 또는 inventory.rejected 발행

장애 상황 시 재시도 후 order.created.DLT로 이동

Notification-Service

재고 결과 이벤트를 소비 → 알림 전송(모의, 로그 출력)

🏗️ 아키텍처 구조
시스템 다이어그램
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

폴더 구조
msa-kafka-lab/
 ├─ docker-compose.yml        # Redpanda + Kafka UI
 ├─ order-service/            # 주문 서비스 (producer)
 ├─ inventory-service/        # 재고 서비스 (consumer+producer, DLQ)
 └─ notification-service/     # 알림 서비스 (consumer)

⚙️ 기술 스택

Java 17 / Spring Boot 3.3.x

Spring Kafka

Docker Compose

Redpanda (Kafka 호환 브로커)

Kafka UI (메시지 시각화)

REST API (Order 서비스)

Kafka Topics

order.created

inventory.reserved

inventory.rejected

order.created.DLT (Dead Letter Queue)

🧑‍💻 코드 리뷰 문서
Order-Service

OrderController

주문 생성 시 KafkaTemplate을 사용해 JSON 이벤트 발행

이벤트 구조 단순(Map 기반) → 학습용으로는 충분, 실무에서는 DTO + Schema Registry 권장

장점

REST → Kafka 이벤트 전환 과정 단순/직관적

비동기 이벤트 발행으로 느슨한 결합 확보

개선 포인트

Outbox 패턴 적용 시 DB 트랜잭션과 이벤트 발행 정합성 확보 가능

이벤트 ID/트레이싱 ID(예: traceId) 추가로 추적성 강화 필요

Inventory-Service

InventoryListener

@KafkaListener 기반 소비자

단순 로직: 짝수 orderId = 성공, 홀수 = 실패

장애 주입: sku="FAIL"일 경우 RuntimeException 발생

에러 처리 (KafkaErrorConfig)

DefaultErrorHandler + DeadLetterPublishingRecoverer

재시도 후 실패 시 .DLT 토픽으로 라우팅

장점

DLQ 활용으로 안정성 보장

메시지 재처리/분석 가능

개선 포인트

DLT 전용 컨슈머 추가하여 운영자 대시보드/재처리 기능 구현 필요

지수 백오프(Exponential BackOff) 적용 고려

Notification-Service

NotificationListener

inventory.reserved / inventory.rejected 이벤트 구독

현재는 단순 로그 출력

장점

비즈니스 확장 지점: 알림(메일, SMS, 푸시 등) 연계 가능

개선 포인트

멱등성 보장 필요(중복 이벤트 방지)

알림 실패 시 재시도 전략 추가 필요

📑 학습 포인트 정리

MSA 환경에서 동기 REST 호출 대신 비동기 이벤트 기반 통신 구조 체험

Kafka Producer/Consumer 기본 코드 작성 및 실행 경험

재시도 + DLQ 처리로 안정성 강화

Kafka UI를 통한 메시지 모니터링 및 디버깅 경험

✅ 결론

이 프로젝트는 MSA + Kafka 학습용으로 가장 단순한 이벤트 흐름을 구현하면서도,
**실무에서 꼭 필요한 개념(재시도, DLQ, 멱등성)**까지 맛볼 수 있도록 구성되었습니다.

다음 확장 과제:

Outbox 패턴 + DB 트랜잭션 연동

Avro + Schema Registry 적용

OpenTelemetry 기반 분산 트레이싱

Notification 실제 구현 (이메일/SMS)

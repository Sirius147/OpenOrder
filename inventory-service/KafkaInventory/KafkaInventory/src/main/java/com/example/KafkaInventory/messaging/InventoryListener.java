package com.example.KafkaInventory.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class InventoryListener {

    private static final Log log = LogFactory.getLog(InventoryListener.class);
    private final KafkaTemplate<String, Object> kafka;
    public InventoryListener(KafkaTemplate<String, Object> kafka) { this.kafka = kafka; }

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void onOrderCreated(Map<String, Object> message) {
        long orderId = Long.parseLong(String.valueOf(message.get("orderId")));

        // ===== 장애 주입 (아이템 첫 번째 sku 가 "FAIL" 이면 일부러 실패) =====
        Map<String, Object> items = (Map<String, Object>) message.get("items");
        if (items != null && !items.isEmpty()) {
            Object sku = items.get("sku");
            if ("FAIL".equals(String.valueOf(sku))) {
                throw new RuntimeException("Injected failure for testing DLT");
            }
        }

        boolean ok = (orderId % 2 == 0); // 데모 규칙: 짝수면 성공

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("orderId", orderId);
        event.put("timestamp", Instant.now().toString());

        if (ok) {
            event.put("status", "RESERVED");
            kafka.send("inventory.reserved", String.valueOf(orderId), event);
            log.info("Reserved: " + orderId);
        } else {
            event.put("status", "REJECTED");
            event.put("reason", "OUT_OF_STOCK");
            kafka.send("inventory.rejected", String.valueOf(orderId), event);
            log.info("Rejected: " + orderId);
        }
    }
}

package com.example.KafkaOrder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        Long orderId = new Random().nextLong(100_000);
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("orderId", orderId);
        event.put("userId", request.get("userId"));
        event.put("items", request.get("items"));
        event.put("createdAt", Instant.now().toString());
        kafkaTemplate.send("order.created", String.valueOf(orderId), event);
        return ResponseEntity.accepted().body(Map.of("orderId", orderId, "status", "PENDING"));
    }
}

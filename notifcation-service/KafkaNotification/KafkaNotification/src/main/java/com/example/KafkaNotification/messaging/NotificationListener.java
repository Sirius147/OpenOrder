package com.example.KafkaNotification.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationListener {
    private static final Log log = LogFactory.getLog(NotificationListener.class);

    @KafkaListener(topics = {"inventory.reserved", "inventory.rejected"}, groupId = "notification-service")
    public void onInventoryResult(Map<String, Object> message) {
        log.info("Send Notification -> " + message);

    }
}


package com.example.KafkaInventory;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

    @Bean
    DefaultErrorHandler defaultErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // 실패 시 <원본토픽>.DLT 로 퍼블리시 (예: order.created -> order.created.DLT)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (rec, ex) -> new TopicPartition(rec.topic() + ".DLT", rec.partition())
        );
        // 1초 간격으로 2번 재시도 후 DLQ 이동
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }
}

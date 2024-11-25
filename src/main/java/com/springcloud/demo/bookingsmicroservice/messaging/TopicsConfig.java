package com.springcloud.demo.bookingsmicroservice.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

//@Configuration
public class TopicsConfig {
//    @Bean
    public NewTopics topics() {
        return new NewTopics(
                TopicBuilder.name("REVIEW_CREATED_TOPIC")
                        .build(),
                TopicBuilder.name("BOOKING_CREATED_TOPIC")
                        .build(),
                TopicBuilder.name("BOOKING_RECEIPT_GENERATED_TOPIC")
                        .build());
    }
}

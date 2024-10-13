package com.springcloud.demo.bookingsmicroservice.messaging;

public interface MessagingProducer {
    void sendMessage(String topic, String message);
}

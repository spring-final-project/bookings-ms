package com.springcloud.demo.bookingsmicroservice.booking.consumer;

import com.springcloud.demo.bookingsmicroservice.booking.dto.PublishBookingEventDTO;
import com.springcloud.demo.bookingsmicroservice.booking.service.BookingService;
import com.springcloud.demo.bookingsmicroservice.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final BookingService bookingService;

    @KafkaListener(topics = "${spring.kafka.topics.BOOKING_RECEIPT_GENERATED_TOPIC}")
    public void updateReceiptUrlEvent(String bookingJson){
        PublishBookingEventDTO booking = JsonUtils.fromJson(bookingJson, PublishBookingEventDTO.class);
        bookingService.updateReceiptUrl(booking);
    }
}

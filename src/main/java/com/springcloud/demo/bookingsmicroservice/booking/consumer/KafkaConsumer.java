package com.springcloud.demo.bookingsmicroservice.booking.consumer;

import com.springcloud.demo.bookingsmicroservice.booking.dto.ResponseBookingDTO;
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
        ResponseBookingDTO booking = JsonUtils.fromJson(bookingJson, ResponseBookingDTO.class);
        bookingService.updateReceiptUrl(booking);
    }
}

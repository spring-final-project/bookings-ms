package com.springcloud.demo.bookingsmicroservice.booking.dto;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBookingDTO {
    private UUID id;
    private String createdAt;
    private String receiptUrl;
    private String checkIn;
    private String checkOut;
    private UUID userId;
    private UUID roomId;
    private BookingStatus status;
    private Integer rating;
    private String review;
}

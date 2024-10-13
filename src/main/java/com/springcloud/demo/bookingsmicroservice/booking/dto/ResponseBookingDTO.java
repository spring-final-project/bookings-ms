package com.springcloud.demo.bookingsmicroservice.booking.dto;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBookingDTO {
    private String id;
    private String createdAt;
    private String receiptUrl;
    private String checkIn;
    private String checkOut;
    private String userId;
    private String roomId;
    private BookingStatus status;
    private Integer rating;
    private String review;
}

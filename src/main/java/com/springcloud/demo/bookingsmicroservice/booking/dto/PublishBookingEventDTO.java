package com.springcloud.demo.bookingsmicroservice.booking.dto;

import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.bookingsmicroservice.client.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublishBookingEventDTO {
    private UUID id;
    private String createdAt;
    private String receiptUrl;
    private String checkIn;
    private String checkOut;
    private BookingStatus status;
    private Integer rating;
    private String review;
    private UserDTO user;
    private RoomDTO room;
}

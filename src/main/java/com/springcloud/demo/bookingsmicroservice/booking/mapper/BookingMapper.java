package com.springcloud.demo.bookingsmicroservice.booking.mapper;

import com.springcloud.demo.bookingsmicroservice.booking.dto.CreateBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.ResponseBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class BookingMapper {

    public static Booking createBookingDtoToBooking(CreateBookingDTO createBookingDTO){
        return Booking
                .builder()
                .roomId(UUID.fromString(createBookingDTO.getRoomId()))
                .status(BookingStatus.BOOKED)
                .build();
    }

    public static ResponseBookingDTO bookingToResponseBookingDto(Booking booking){
        OffsetDateTime createdAt = booking.getCreatedAt().withOffsetSameInstant(booking.getCheckIn().getOffset());

        return ResponseBookingDTO
                .builder()
                .id(booking.getId())
                .checkIn(booking.getCheckIn().toString())
                .checkOut(booking.getCheckOut().toString())
                .roomId(booking.getRoomId())
                .userId(booking.getUserId())
                .createdAt(createdAt.toString())
                .status(booking.getStatus())
                .receiptUrl(booking.getReceiptUrl())
                .review(booking.getReview())
                .rating(booking.getRating())
                .build();
    }
}

package com.springcloud.demo.bookingsmicroservice.booking.mapper;

import com.springcloud.demo.bookingsmicroservice.booking.dto.CreateBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.ResponseBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;

import java.util.UUID;

public class BookingMapper {

    public static Booking createBookingDtoToBooking(CreateBookingDTO createBookingDTO){
        return Booking
                .builder()
                .checkIn(createBookingDTO.getCheckIn())
                .checkOut(createBookingDTO.getCheckOut())
                .roomId(UUID.fromString(createBookingDTO.getRoomId()))
                .status(BookingStatus.BOOKED)
                .build();
    }

    public static ResponseBookingDTO bookingToResponseBookingDto(Booking booking){
        return ResponseBookingDTO
                .builder()
                .id(booking.getId())
                .checkIn(booking.getCheckIn().toString())
                .checkOut(booking.getCheckOut().toString())
                .roomId(booking.getRoomId())
                .userId(booking.getUserId())
                .createdAt(booking.getCreatedAt().toString())
                .status(booking.getStatus())
                .receiptUrl(booking.getReceiptUrl())
                .review(booking.getReview())
                .rating(booking.getRating())
                .build();
    }
}

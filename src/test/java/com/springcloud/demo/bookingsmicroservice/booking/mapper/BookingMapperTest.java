package com.springcloud.demo.bookingsmicroservice.booking.mapper;

import com.springcloud.demo.bookingsmicroservice.booking.dto.CreateBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.ResponseBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import static org.assertj.core.api.Assertions.*;

import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

class BookingMapperTest {

    @Test
    void createBookingDtoToBooking(){
        CreateBookingDTO createBookingDTO = CreateBookingDTO
                .builder()
                .checkIn(OffsetDateTime.now().toString())
                .checkOut(OffsetDateTime.now().plusDays(2).toString())
                .roomId(UUID.randomUUID().toString())
                .build();

        Booking booking = BookingMapper.createBookingDtoToBooking(createBookingDTO);

//        assertThat(booking.getCheckIn()).isEqualTo(createBookingDTO.getCheckIn());
//        assertThat(booking.getCheckOut()).isEqualTo(createBookingDTO.getCheckOut());
        assertThat(booking.getRoomId().toString()).isEqualTo(createBookingDTO.getRoomId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BOOKED);
    }

    @Test
    void bookingToResponseBookingDto(){
        Booking booking = Booking
                .builder()
                .id(UUID.randomUUID())
                .createdAt(OffsetDateTime.now())
                .checkIn(OffsetDateTime.now())
                .checkOut(OffsetDateTime.now().plusDays(2))
                .userId(UUID.randomUUID())
                .roomId(UUID.randomUUID())
                .status(BookingStatus.BOOKED)
                .rating(5)
                .review("Review")
                .receiptUrl("http://localhost:8080/receipt.pdf")
                .build();

        ResponseBookingDTO response = BookingMapper.bookingToResponseBookingDto(booking);

        assertThat(response.getId()).isEqualTo(booking.getId());
        assertThat(response.getCreatedAt()).isEqualTo(booking.getCreatedAt().toString());
        assertThat(response.getCheckIn()).isEqualTo(booking.getCheckIn().toString());
        assertThat(response.getCheckOut()).isEqualTo(booking.getCheckOut().toString());
        assertThat(response.getUserId()).isEqualTo(booking.getUserId());
        assertThat(response.getRoomId()).isEqualTo(booking.getRoomId());
        assertThat(response.getStatus()).isEqualTo(booking.getStatus());
        assertThat(response.getRating()).isEqualTo(booking.getRating());
        assertThat(response.getReview()).isEqualTo(booking.getReview());
        assertThat(response.getReceiptUrl()).isEqualTo(booking.getReceiptUrl());
    }
}
package com.springcloud.demo.bookingsmicroservice.booking.service;

import com.springcloud.demo.bookingsmicroservice.booking.dto.*;
import com.springcloud.demo.bookingsmicroservice.booking.mapper.BookingMapper;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingRepository;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingSpecification;
import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.bookingsmicroservice.client.users.UserClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.users.UserDTO;
import com.springcloud.demo.bookingsmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.bookingsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.bookingsmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.bookingsmicroservice.utils.JsonUtils;
import com.springcloud.demo.bookingsmicroservice.messaging.MessagingProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSpecification bookingSpecification;
    private final RoomClientImpl roomClient;
    private final UserClientImpl userClient;
    private final MessagingProducer messagingProducer;

    @Value("${spring.kafka.topics.BOOKING_CREATED_TOPIC}")
    private String bookingCreatedTopic;

    @Value("${spring.kafka.topics.REVIEW_CREATED_TOPIC}")
    private String reviewCreatedTopic;

    public ResponseBookingDTO create(CreateBookingDTO createBookingDTO, String idUserLogged) {

        RoomDTO room = roomClient.findById(createBookingDTO.getRoomId());

        OffsetDateTime checkIn;
        OffsetDateTime checkOut;

        try {
            checkIn = OffsetDateTime.parse(createBookingDTO.getCheckIn());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("checkIn is not a valid date");
        }
        try {
            checkOut = OffsetDateTime.parse(createBookingDTO.getCheckOut());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("checkOut is not a valid date");
        }

        if(checkIn.isBefore(OffsetDateTime.now())){
            throw new BadRequestException("checkIn cannot be before current time");
        }

        if (checkIn.isAfter(checkOut)) {
            throw new BadRequestException("checkIn cannot be after checkOut");
        }

        Booking[] bookingsInSameRange = bookingRepository.findBookingsByRange(
                checkIn,
                checkOut,
                UUID.fromString(idUserLogged),
                UUID.fromString(createBookingDTO.getRoomId())
        );

        if (bookingsInSameRange.length > 0) {
            Booking bookingInSameRange = bookingsInSameRange[0];
            if (bookingInSameRange.getUserId().toString().equals(idUserLogged)) {
                throw new ForbiddenException("User already has any booking at same time");
            }
            if (bookingInSameRange.getRoomId().toString().equals(createBookingDTO.getRoomId())) {
                throw new ForbiddenException("Room already booked at same time");
            }
        }

        UserDTO user = userClient.findById(idUserLogged);
        UserDTO owner = userClient.findById(room.getOwnerId());
        room.setOwner(owner);

        Booking booking = BookingMapper.createBookingDtoToBooking(createBookingDTO);
        booking.setUserId(UUID.fromString(idUserLogged));
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);

        booking = bookingRepository.save(booking);

        messagingProducer.sendMessage(bookingCreatedTopic, JsonUtils.toJson(BookingMapper.bookingToPublishBookingDto(booking,room,user)));

        return BookingMapper.bookingToResponseBookingDto(booking);
    }

    public List<ResponseBookingDTO> findAll(FilterBookingDTO filters) {
        Pageable pageable = PageRequest.of(filters.getPage() - 1, filters.getLimit());

        List<Booking> bookings = bookingRepository.findAll(
                bookingSpecification.withFilters(filters),
                pageable
        ).getContent();

        return bookings.stream().map(BookingMapper::bookingToResponseBookingDto).toList();
    }

    public ResponseBookingDTO findById(String id) {
        Booking booking = bookingRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found booking with id:" + id));

        return BookingMapper.bookingToResponseBookingDto(booking);
    }

    public ResponseBookingDTO updateStatus(String id, UpdateStatusDTO updateStatusDTO, String idUserLogged) {
        BookingStatus validStatus;
        try {
            validStatus = BookingStatus.valueOf(updateStatusDTO.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(updateStatusDTO.getStatus() + " is not valid status. " + Arrays.toString(BookingStatus.values()));
        }

        Booking booking = bookingRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found booking with id:" + id));

        RoomDTO roomOfBooking = roomClient.findById(booking.getRoomId().toString());

        if (!roomOfBooking.getOwnerId().equals(idUserLogged)) {
            throw new ForbiddenException("Not have permission to update booking of room that belong to another user");
        }

        booking.setStatus(validStatus);

        booking = bookingRepository.save(booking);

        return BookingMapper.bookingToResponseBookingDto(booking);
    }

    public ResponseBookingDTO review(String id, CreateReviewDTO createReviewDTO, String idUserLogged) {
        Booking booking = bookingRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found booking with id:" + id));

        if (!booking.getUserId().toString().equals(idUserLogged)) {
            throw new ForbiddenException("Not have permission to review booking that belong to another user");
        }

        if (booking.getRating() != null) {
            throw new ForbiddenException("Booking already reviewed");
        }

        booking.setReview(createReviewDTO.getReview());
        booking.setRating(createReviewDTO.getRating());

        booking = bookingRepository.save(booking);

        ResponseBookingDTO responseBookingDTO = BookingMapper.bookingToResponseBookingDto(booking);

        messagingProducer.sendMessage(reviewCreatedTopic, JsonUtils.toJson(responseBookingDTO));

        return responseBookingDTO;
    }

    public void updateReceiptUrl(PublishBookingEventDTO booking) {
        Optional<Booking> bookingFound = bookingRepository.findById(booking.getId());

        if (bookingFound.isPresent()) {
            bookingFound.get().setReceiptUrl(booking.getReceiptUrl());
            bookingRepository.save(bookingFound.get());
        }
    }
}

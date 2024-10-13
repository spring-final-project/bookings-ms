package com.springcloud.demo.bookingsmicroservice.booking.service;

import com.springcloud.demo.bookingsmicroservice.booking.dto.*;
import com.springcloud.demo.bookingsmicroservice.booking.mapper.BookingMapper;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingRepository;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingSpecification;
import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSpecification bookingSpecification;
    private final RoomClientImpl roomClient;
    private final MessagingProducer messagingProducer;

    @Value("${spring.kafka.topics.BOOKING_CREATED_TOPIC}")
    private String bookingCreatedTopic;

    @Value("${spring.kafka.topics.REVIEW_CREATED_TOPIC}")
    private String reviewCreatedTopic;

    public ResponseBookingDTO create(CreateBookingDTO createBookingDTO, String idUserLogged) {

        //        Check exist room
        roomClient.findById(createBookingDTO.getRoomId());

        if(createBookingDTO.getCheckIn().isAfter(createBookingDTO.getCheckOut())){
            throw new BadRequestException("checkIn cannot be after checkOut");
        }

        Optional<Booking> bookingInSameRange = bookingRepository.findBookingsByRange(
                createBookingDTO.getCheckIn(),
                createBookingDTO.getCheckOut(),
                idUserLogged,
                createBookingDTO.getRoomId()
        );

        if(bookingInSameRange.isPresent()){
            if(bookingInSameRange.get().getUserId().equals(idUserLogged)){
                throw new ForbiddenException("User already has any booking at same time");
            }
            if(bookingInSameRange.get().getRoomId().equals(createBookingDTO.getRoomId())){
                throw new ForbiddenException("Room already booked at same time");
            }
        }

        Booking booking = BookingMapper.createBookingDtoToBooking(createBookingDTO);
        booking.setUserId(idUserLogged);

        booking = bookingRepository.save(booking);

        ResponseBookingDTO responseBookingDTO = BookingMapper.bookingToResponseBookingDto(booking);

        messagingProducer.sendMessage(bookingCreatedTopic, JsonUtils.toJson(responseBookingDTO));

        return responseBookingDTO;
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
                .findById(id)
                .orElseThrow(()-> new NotFoundException("Not found booking with id:" + id));

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
                .findById(id)
                .orElseThrow(()-> new NotFoundException("Not found booking with id:" + id));

        RoomDTO roomOfBooking = roomClient.findById(booking.getRoomId());

        if(!roomOfBooking.getOwnerId().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to update booking of room that belong to another user");
        }

        booking.setStatus(validStatus);

        booking = bookingRepository.save(booking);

        return BookingMapper.bookingToResponseBookingDto(booking);
    }

    public ResponseBookingDTO review(String id, CreateReviewDTO createReviewDTO, String idUserLogged) {
        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(()-> new NotFoundException("Not found booking with id:" + id));

        if(!booking.getUserId().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to review booking that belong to another user");
        }

        if(booking.getRating() != null){
            throw new ForbiddenException("Booking already reviewed");
        }

        booking.setReview(createReviewDTO.getReview());
        booking.setRating(createReviewDTO.getRating());

        booking = bookingRepository.save(booking);

        ResponseBookingDTO responseBookingDTO = BookingMapper.bookingToResponseBookingDto(booking);

        messagingProducer.sendMessage(reviewCreatedTopic, JsonUtils.toJson(responseBookingDTO));

        return responseBookingDTO;
    }

    public void updateReceiptUrl(ResponseBookingDTO booking){
        Optional<Booking> bookingFound = bookingRepository.findById(booking.getId());

        if(bookingFound.isPresent()){
            bookingFound.get().setReceiptUrl(booking.getReceiptUrl());
            bookingRepository.save(bookingFound.get());
        }
    }
}

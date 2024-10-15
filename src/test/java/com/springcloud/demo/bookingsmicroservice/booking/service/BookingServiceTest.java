package com.springcloud.demo.bookingsmicroservice.booking.service;

import com.springcloud.demo.bookingsmicroservice.booking.dto.*;
import com.springcloud.demo.bookingsmicroservice.booking.mapper.BookingMapper;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingRepository;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingSpecification;

import static org.assertj.core.api.Assertions.*;

import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.bookingsmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.bookingsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.bookingsmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.bookingsmicroservice.messaging.MessagingProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSpecification bookingSpecification;

    @Mock
    private RoomClientImpl roomClient;

    @Mock
    private MessagingProducer messagingProducer;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(bookingService, "bookingCreatedTopic", "topic1");
        ReflectionTestUtils.setField(bookingService, "reviewCreatedTopic", "topic2");
    }

    @Nested
    class Create {

        CreateBookingDTO createBookingDTO;

        @BeforeEach
        void setup() {
            createBookingDTO = CreateBookingDTO
                    .builder()
                    .checkIn(LocalDateTime.now())
                    .checkOut(LocalDateTime.now())
                    .roomId(UUID.randomUUID().toString())
                    .build();
        }

        @Test
        void createBooking() {

            Booking bookingSaved = Booking.builder()
                    .id(UUID.randomUUID())
                    .checkIn(createBookingDTO.getCheckIn())
                    .checkOut(createBookingDTO.getCheckOut())
                    .roomId(UUID.fromString(createBookingDTO.getRoomId()))
                    .userId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .status(BookingStatus.BOOKED)
                    .build();

            given(bookingRepository.findBookingsByRange(any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(UUID.class))).willReturn(Optional.empty());
            given(bookingRepository.save(any(Booking.class))).willReturn(bookingSaved);
            willDoNothing().given(messagingProducer).sendMessage(anyString(), anyString());

            ResponseBookingDTO response = bookingService.create(createBookingDTO, UUID.randomUUID().toString());

            verify(bookingRepository).findBookingsByRange(eq(createBookingDTO.getCheckIn()), eq(createBookingDTO.getCheckOut()), any(UUID.class), eq(UUID.fromString(createBookingDTO.getRoomId())));
            verify(bookingRepository).save(argThat(dto ->
                    Objects.equals(dto.getCheckIn(), createBookingDTO.getCheckIn()) &&
                            Objects.equals(dto.getCheckOut(), createBookingDTO.getCheckOut()) &&
                            Objects.equals(dto.getRoomId(), UUID.fromString(createBookingDTO.getRoomId())) &&
                            dto.getUserId() != null
            ));
            assertThat(response).isNotNull();
        }

        @Test
        void errorWhenAlreadyExistOtherBookingInSameRange() {
            Booking bookingAtSameTime = Booking.builder()
                    .status(BookingStatus.BOOKED)
                    .userId(UUID.randomUUID())
                    .build();

            given(bookingRepository.findBookingsByRange(any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(UUID.class))).willReturn(Optional.of(bookingAtSameTime));

            ForbiddenException e = Assertions.assertThrows(ForbiddenException.class, () -> {
                bookingService.create(createBookingDTO, bookingAtSameTime.getUserId().toString());
            });

            verify(bookingRepository).findBookingsByRange(eq(createBookingDTO.getCheckIn()), eq(createBookingDTO.getCheckOut()), any(UUID.class), eq(UUID.fromString(createBookingDTO.getRoomId())));
            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }

    @Nested
    class FindAll {

        FilterBookingDTO filters;
        List<Booking> bookingsFound;

        @BeforeEach
        void setup() {
            filters = new FilterBookingDTO();
            Booking booking1 = Booking.builder().id(UUID.randomUUID()).checkIn(LocalDateTime.now()).checkOut(LocalDateTime.now()).roomId(UUID.randomUUID()).userId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
            Booking booking2 = Booking.builder().id(UUID.randomUUID()).checkIn(LocalDateTime.now()).checkOut(LocalDateTime.now()).roomId(UUID.randomUUID()).userId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
            bookingsFound = List.of(booking1, booking2);
        }

        @Test
        void findAllBookings() {
            given(bookingSpecification.withFilters(any(FilterBookingDTO.class))).willReturn((root, query, builder) -> builder.and());
            given(bookingRepository.findAll((Specification<Booking>) any(), any(Pageable.class))).willReturn(new PageImpl<>(bookingsFound));

            List<ResponseBookingDTO> response = bookingService.findAll(filters);

            verify(bookingSpecification).withFilters(filters);
            verify(bookingRepository).findAll(
                    eq(bookingSpecification.withFilters(filters)),
                    eq(PageRequest.of(0, 20))
            );
            assertThat(response).isNotNull();
        }

        @Test
        void findAllWithFilters() {
            filters.setPage(2);
            filters.setLimit(10);
            filters.setStatus(BookingStatus.BOOKED);
            filters.setMinRating(2);
            filters.setMaxRating(4);
            filters.setRoomId(UUID.randomUUID().toString());
            filters.setUserId(UUID.randomUUID().toString());

            given(bookingSpecification.withFilters(any(FilterBookingDTO.class))).willReturn((root, query, builder) -> builder.and());
            given(bookingRepository.findAll((Specification<Booking>) any(), any(Pageable.class))).willReturn(new PageImpl<>(bookingsFound));

            List<ResponseBookingDTO> response = bookingService.findAll(filters);

            verify(bookingSpecification).withFilters(argThat(args ->
                    Objects.equals(args.getPage(), filters.getPage()) &&
                            Objects.equals(args.getLimit(), filters.getLimit()) &&
                            args.getStatus().name().equals(filters.getStatus().name()) &&
                            Objects.equals(args.getMinRating(), filters.getMinRating()) &&
                            Objects.equals(args.getMaxRating(), filters.getMaxRating()) &&
                            Objects.equals(args.getRoomId(), filters.getRoomId()) &&
                            Objects.equals(args.getUserId(), filters.getUserId())
            ));
            verify(bookingRepository).findAll(
                    eq(bookingSpecification.withFilters(filters)),
                    eq(PageRequest.of(1, 10))
            );
            assertThat(response).isNotNull();
        }
    }

    @Nested
    class FindById {
        @Test
        void findById() {
            String idToFind = UUID.randomUUID().toString();
            Booking bookingFound = Booking.builder()
                    .id(UUID.randomUUID())
                    .checkIn(LocalDateTime.now())
                    .checkOut(LocalDateTime.now().plusDays(2))
                    .roomId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .status(BookingStatus.BOOKED)
                    .build();

            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.of(bookingFound));

            ResponseBookingDTO response = bookingService.findById(idToFind);

            verify(bookingRepository).findById(UUID.fromString(idToFind));
            assertThat(response).isNotNull();
        }

        @Test
        void errorWhenNotFoundBookingById() {
            String idToFind = UUID.randomUUID().toString();

            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                bookingService.findById(idToFind);
            });

            verify(bookingRepository).findById(UUID.fromString(idToFind));
            assertThat(e.getMessage()).contains("Not found booking with id");
        }
    }

    @Nested
    class UpdateStatus {
        @Test
        void updateStatus() {
            String idToFind = UUID.randomUUID().toString();
            UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO(BookingStatus.IN_USE.toString());
            RoomDTO roomDTO = RoomDTO.builder().id(UUID.randomUUID()).ownerId(UUID.randomUUID().toString()).build();
            Booking bookingFound = Booking.builder()
                    .id(UUID.randomUUID())
                    .checkIn(LocalDateTime.now())
                    .checkOut(LocalDateTime.now().plusDays(2))
                    .roomId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .status(BookingStatus.BOOKED)
                    .build();
            Booking updatedBooking = Booking.builder()
                    .id(UUID.randomUUID())
                    .checkIn(LocalDateTime.now())
                    .checkOut(LocalDateTime.now().plusDays(2))
                    .roomId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .status(BookingStatus.DELIVERED)
                    .build();


            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.of(bookingFound));
            given(roomClient.findById(anyString())).willReturn(roomDTO);
            given(bookingRepository.save(any(Booking.class))).willReturn(updatedBooking);

            ResponseBookingDTO response = bookingService.updateStatus(idToFind, updateStatusDTO, roomDTO.getOwnerId());

            verify(bookingRepository).findById(UUID.fromString(idToFind));
            verify(bookingRepository).save(bookingFound);
            verify(bookingRepository).save(argThat(args -> args.getStatus().name().equals(updateStatusDTO.getStatus())));
            assertThat(response).isNotNull();
        }

        @Test
        void errorWhenNotFoundBookingById() {
            String idToFind = UUID.randomUUID().toString();
            UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO(BookingStatus.IN_USE.toString());

            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                bookingService.updateStatus(idToFind, updateStatusDTO, UUID.randomUUID().toString());
            });

            verify(bookingRepository).findById(UUID.fromString(idToFind));
            verify(bookingRepository, never()).save(any());
            assertThat(e.getMessage()).contains("Not found booking with id");
        }

        @Test
        void errorWhenStatusIsNotValid() {
            String idToFind = UUID.randomUUID().toString();
            UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO("OTHER_STATUS");

            BadRequestException e = Assertions.assertThrows(BadRequestException.class, () -> {
                bookingService.updateStatus(idToFind, updateStatusDTO, UUID.randomUUID().toString());
            });

            verify(bookingRepository, never()).findById(UUID.fromString(idToFind));
            verify(bookingRepository, never()).save(any());
            assertThat(e.getMessage()).contains("is not valid status");
        }
    }

    @Nested
    class Review {
        @Test
        void createReview() {
            String idToUpdate = UUID.randomUUID().toString();
            CreateReviewDTO createReviewDTO = new CreateReviewDTO(5, "review");
            Booking bookingFound = Booking.builder()
                    .id(UUID.randomUUID())
                    .checkIn(LocalDateTime.now())
                    .checkOut(LocalDateTime.now().plusDays(2))
                    .roomId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .status(BookingStatus.DELIVERED)
                    .build();
            Booking updatedBooking = Booking.builder()
                    .id(bookingFound.getId())
                    .checkIn(bookingFound.getCheckIn())
                    .checkOut(bookingFound.getCheckOut())
                    .roomId(bookingFound.getRoomId())
                    .userId(bookingFound.getUserId())
                    .createdAt(bookingFound.getCreatedAt())
                    .status(bookingFound.getStatus())
                    .review(createReviewDTO.getReview())
                    .rating(createReviewDTO.getRating())
                    .build();

            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.of(bookingFound));
            given(bookingRepository.save(any(Booking.class))).willReturn(updatedBooking);

            ResponseBookingDTO response = bookingService.review(idToUpdate, createReviewDTO, bookingFound.getUserId().toString());

            verify(bookingRepository).findById(UUID.fromString(idToUpdate));
            verify(bookingRepository).save(bookingFound);
            verify(bookingRepository).save(argThat(args ->
                    Objects.equals(args.getRating(), createReviewDTO.getRating()) &&
                            args.getReview().equals(createReviewDTO.getReview())
            ));
            assertThat(response).isNotNull();
        }

        @Test
        void errorWhenNotFoundBookingById() {
            String idToUpdate = UUID.randomUUID().toString();
            CreateReviewDTO createReviewDTO = new CreateReviewDTO(5, "review");

            given(bookingRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()-> {
                bookingService.review(idToUpdate, createReviewDTO, UUID.randomUUID().toString());
            });

            verify(bookingRepository).findById(UUID.fromString(idToUpdate));
            verify(bookingRepository, never()).save(any());
            assertThat(e.getMessage()).contains("Not found booking with id");
        }
    }
}
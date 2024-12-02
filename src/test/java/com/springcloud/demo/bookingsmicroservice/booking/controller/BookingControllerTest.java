package com.springcloud.demo.bookingsmicroservice.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.bookingsmicroservice.booking.dto.*;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.booking.service.BookingService;
import com.springcloud.demo.bookingsmicroservice.monitoring.TracingExceptions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@WebMvcTest
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private TracingExceptions tracingExceptions;

    @Nested
    class Create {

        CreateBookingDTO createBookingDTO;

        @BeforeEach
        void setup() {
            createBookingDTO = new CreateBookingDTO();
        }

        @Test
        void createBooking() throws Exception {
            createBookingDTO.setCheckIn(OffsetDateTime.now().plusDays(1).toString());
            createBookingDTO.setCheckOut(OffsetDateTime.now().plusDays(3).toString());
            createBookingDTO.setRoomId(UUID.randomUUID().toString());

            given(bookingService.create(any(CreateBookingDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(new ResponseBookingDTO())));
            verify(bookingService).create(argThat(args ->
                    args.getCheckIn().equals(createBookingDTO.getCheckIn()) &&
                            args.getCheckOut().equals(createBookingDTO.getCheckOut()) &&
                            args.getRoomId().equals(createBookingDTO.getRoomId())
            ), anyString());
        }

        @Test
        void errorWhenMissingFields() throws Exception {

            given(bookingService.create(any(CreateBookingDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(3));
        }

        @Test
        void errorWhenNotSendBody() throws Exception {

            given(bookingService.create(any(CreateBookingDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("body is missing")));
        }
    }

    @Nested
    class FindAll {
        @Test
        void findAll() throws Exception {
            given(bookingService.findAll(any(FilterBookingDTO.class))).willReturn(List.of(new ResponseBookingDTO()));

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
            verify(bookingService).findAll(new FilterBookingDTO());
        }

        @Test
        void findAllWithFilters() throws Exception {
            String userId = UUID.randomUUID().toString();
            String roomId = UUID.randomUUID().toString();
            given(bookingService.findAll(any(FilterBookingDTO.class))).willReturn(List.of(new ResponseBookingDTO()));

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("page", "2")
                                    .queryParam("limit", "10")
                                    .queryParam("userId", userId)
                                    .queryParam("roomId", roomId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
            verify(bookingService).findAll(argThat(args ->
                    args.getPage() == 2 &&
                            args.getLimit() == 10 &&
                            args.getUserId().equals(userId) &&
                            args.getRoomId().equals(roomId)
            ));
        }

        @Test
        void errorWhenFiltersNotValid() throws Exception {
            String fakeId = "abcd1234";
            given(bookingService.findAll(any(FilterBookingDTO.class))).willReturn(List.of(new ResponseBookingDTO()));

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("page", "-2")
                                    .queryParam("limit", "-10")
                                    .queryParam("userId", fakeId)
                                    .queryParam("roomId", fakeId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(4));
            verify(bookingService, never()).findAll(any());
        }
    }

    @Nested
    class FindById {
        @Test
        void findById() throws Exception {
            String id = UUID.randomUUID().toString();
            given(bookingService.findById(anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings/" + id))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(new ResponseBookingDTO())));
            verify(bookingService).findById(id);
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String id = "abcd1234";
            given(bookingService.findById(anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings/" + id))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(bookingService, never()).findById(anyString());
        }
    }

    @Nested
    class UpdateStatus {

        UpdateStatusDTO updateStatusDTO;

        @BeforeEach
        void setup() {
            updateStatusDTO = new UpdateStatusDTO();
        }

        @Test
        void updateStatus() throws Exception {
            String idToUpdate = UUID.randomUUID().toString();
            updateStatusDTO.setStatus(BookingStatus.IN_USE.name());
            given(bookingService.updateStatus(anyString(), any(UpdateStatusDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .patch("/api/bookings/" + idToUpdate + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(new ResponseBookingDTO())));
            verify(bookingService).updateStatus(eq(idToUpdate), eq(updateStatusDTO), anyString());
        }

        @Test
        void errorWhenNotReceiveStatus() throws Exception {
            String idToUpdate = UUID.randomUUID().toString();
            given(bookingService.updateStatus(anyString(), any(UpdateStatusDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .patch("/api/bookings/" + idToUpdate + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(bookingService, never()).updateStatus(anyString(), any(UpdateStatusDTO.class), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idToUpdate = "abcd1234";
            updateStatusDTO.setStatus(BookingStatus.IN_USE.name());
            given(bookingService.updateStatus(anyString(), any(UpdateStatusDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .patch("/api/bookings/" + idToUpdate + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(bookingService, never()).updateStatus(anyString(), any(UpdateStatusDTO.class), anyString());
        }
    }

    @Nested
    class Review {

        CreateReviewDTO createReviewDTO;

        @BeforeEach
        void setup() {
            createReviewDTO = new CreateReviewDTO();
        }

        @Test
        void review() throws Exception {
            String idToReview = UUID.randomUUID().toString();
            createReviewDTO.setRating(4);
            createReviewDTO.setReview("review");

            given(bookingService.review(anyString(), any(CreateReviewDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/bookings/" + idToReview + "/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReviewDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(new ResponseBookingDTO())));
            verify(bookingService).review(eq(idToReview), eq(createReviewDTO), anyString());
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            String idToReview = UUID.randomUUID().toString();

            given(bookingService.review(anyString(), any(CreateReviewDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/bookings/" + idToReview + "/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReviewDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(bookingService, never()).review(anyString(), any(CreateReviewDTO.class), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idToReview = "abcd1234";
            createReviewDTO.setRating(4);
            createReviewDTO.setReview("review");

            given(bookingService.review(anyString(), any(CreateReviewDTO.class), anyString())).willReturn(new ResponseBookingDTO());

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/bookings/" + idToReview + "/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReviewDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(bookingService, never()).review(anyString(), any(CreateReviewDTO.class), anyString());
        }
    }

}
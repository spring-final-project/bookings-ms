package com.springcloud.demo.bookingsmicroservice.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.springcloud.demo.bookingsmicroservice.booking.dto.CreateBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.CreateReviewDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.FilterBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.dto.UpdateStatusDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import com.springcloud.demo.bookingsmicroservice.booking.repository.BookingRepository;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.bookingsmicroservice.messaging.MessagingProducer;
import org.hamcrest.Matchers;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingTestIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomClientImpl roomClient;

    @MockBean
    private MessagingProducer messagingProducer;

    List<Booking> bookings;

    @BeforeEach
    void setup(){
        bookingRepository.deleteAll();

        Booking booking1 = Booking
                .builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(3))
                .userId(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .status(BookingStatus.BOOKED)
                .build();

        Booking booking2 = Booking
                .builder()
                .checkIn(LocalDateTime.now().plusDays(4))
                .checkOut(LocalDateTime.now().plusDays(7))
                .userId(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .status(BookingStatus.BOOKED)
                .build();

        Booking booking3 = Booking
                .builder()
                .checkIn(LocalDateTime.now().minusDays(4))
                .checkOut(LocalDateTime.now().minusDays(1))
                .userId(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .status(BookingStatus.DELIVERED)
                .review("Muy buena atención")
                .rating(5)
                .build();

        bookings = bookingRepository.saveAll(List.of(booking1,booking2,booking3));
    }

    @Nested
    class Create {

        CreateBookingDTO createBookingDTO;

        @BeforeEach
        void setup(){
            createBookingDTO = new CreateBookingDTO();
        }

        @Test
        void createBooking() throws Exception {

            createBookingDTO.setCheckIn(LocalDateTime.now().plusDays(7));
            createBookingDTO.setCheckOut(LocalDateTime.now().plusDays(10));
            createBookingDTO.setRoomId(UUID.randomUUID().toString());

            String idUserLogged = UUID.randomUUID().toString();

            MvcResult result = mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                                    .header("X-UserId", idUserLogged)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.checkIn").value(createBookingDTO.getCheckIn().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.checkOut").value(createBookingDTO.getCheckOut().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roomId").value(createBookingDTO.getRoomId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(idUserLogged))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(BookingStatus.BOOKED.name()))
                    .andReturn();

            String idBookingCreated = JsonPath.parse( result.getResponse().getContentAsString()).read("$.id");
            Booking bookingCreated = bookingRepository.findById(idBookingCreated).orElseThrow(()-> new AssertionFailure("Booking not created in DB"));

            assertThat(bookingCreated.getCheckIn().toLocalDate()).isEqualTo(createBookingDTO.getCheckIn().toLocalDate());
            assertThat(bookingCreated.getCheckIn().getHour()).isEqualTo(createBookingDTO.getCheckIn().getHour());
            assertThat(bookingCreated.getCheckIn().getMinute()).isEqualTo(createBookingDTO.getCheckIn().getMinute());
            assertThat(bookingCreated.getCheckIn().getSecond()).isEqualTo(createBookingDTO.getCheckIn().getSecond());
            assertThat(bookingCreated.getCheckOut().toLocalDate()).isEqualTo(createBookingDTO.getCheckOut().toLocalDate());
            assertThat(bookingCreated.getCheckOut().getHour()).isEqualTo(createBookingDTO.getCheckOut().getHour());
            assertThat(bookingCreated.getCheckOut().getMinute()).isEqualTo(createBookingDTO.getCheckOut().getMinute());
            assertThat(bookingCreated.getCheckOut().getSecond()).isEqualTo(createBookingDTO.getCheckOut().getSecond());
            assertThat(bookingCreated.getRoomId()).isEqualTo(createBookingDTO.getRoomId());
            assertThat(bookingCreated.getUserId()).isEqualTo(idUserLogged);
            assertThat(bookingCreated.getStatus()).isEqualTo(BookingStatus.BOOKED);
        }

        @Test
        void errorWhenAlreadyExistBookingAtSameTime() throws Exception {

            createBookingDTO.setCheckIn(LocalDateTime.now().plusDays(1));
            createBookingDTO.setCheckOut(LocalDateTime.now().plusDays(2));
            createBookingDTO.setRoomId(bookings.getFirst().getRoomId());

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.FORBIDDEN.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Room already booked at same time")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists());

            List<Booking> bookingList = bookingRepository.findAll();
            assertThat(bookingList).hasSameSizeAs(bookings);
        }

        @Test
        void errorWhenCheckOutIsBeforeCheckIn() throws Exception {

            createBookingDTO.setCheckIn(LocalDateTime.now().plusDays(10));
            createBookingDTO.setCheckOut(LocalDateTime.now().plusDays(7));
            createBookingDTO.setRoomId(UUID.randomUUID().toString());

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("checkIn cannot be after checkOut")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists());

            List<Booking> bookingList = bookingRepository.findAll();
            assertThat(bookingList).hasSameSizeAs(bookings);
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createBookingDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(3));

            List<Booking> bookingList = bookingRepository.findAll();
            assertThat(bookingList).hasSameSizeAs(bookings);
        }
    }

    @Nested
    class FindAll {

        FilterBookingDTO filters;

        @BeforeEach
        void setup(){
            filters = new FilterBookingDTO();
        }

        @Test
        void findAll() throws Exception {
            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3));
        }

        @Test
        void findAllFilterByRoomId() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("roomId", bookings.getFirst().getRoomId())

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
        }

        @Test
        void findAllFilterByUserId() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("userId", bookings.getFirst().getUserId())

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
        }

        @Test
        void findAllFilterByStatus() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("status", BookingStatus.BOOKED.name())

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
        }

        @Test
        void findAllFilterByMinRating() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("minRating", "3")

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
        }

        @Test
        void findAllFilterByMaxRating() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("maxRating", "4")

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
        }

        @Test
        void findAllFilterByCheckIn() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("checkIn", LocalDateTime.now().minusDays(1).toString())

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
        }

        @Test
        void findAllFilterByCheckOut() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("checkOut", LocalDateTime.now().plusDays(4).toString())

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
        }

        @Test
        void findAllWithPagination() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("page", "2")
                                    .queryParam("limit", "2")

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
        }

        @Test
        void errorWhenFieldsHasNoValidFormat() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .get("/api/bookings")
                                    .queryParam("userId", "abcd1234")
                                    .queryParam("roomId", "abcd1234")
                                    .queryParam("minRating", "-1")
                                    .queryParam("maxRating", "20")
                                    .queryParam("page", "-1")
                                    .queryParam("limit", "-1")

                    )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(6));
        }
    }

    @Nested
    class FindById {
        @Test
        void findById() throws Exception {
            String idToFind = bookings.getLast().getId();

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToFind));
        }

        @Test
        void errorWhenNotFoundBookingById() throws Exception {
            String idToFind = UUID.randomUUID().toString();

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found booking with id")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idToFind = "abcd1234";

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/bookings/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
        }
    }

    @Nested
    class UpdateStatus {

        UpdateStatusDTO updateStatusDTO;

        @BeforeEach
        void setup(){
            updateStatusDTO = new UpdateStatusDTO();
        }

        @Test
        void updateStatus() throws Exception {
            String idToUpdate = bookings.getFirst().getId();
            updateStatusDTO.setStatus(BookingStatus.CANCELLED.name());

            RoomDTO roomDTO = RoomDTO.builder().id(UUID.randomUUID()).ownerId(UUID.randomUUID().toString()).build();
            given(roomClient.findById(anyString())).willReturn(roomDTO);

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .patch("/api/bookings/" + idToUpdate + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateStatusDTO))
                                    .header("X-UserId", roomDTO.getOwnerId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToUpdate))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(updateStatusDTO.getStatus()));

            Booking bookingUpdated = bookingRepository.findById(idToUpdate).orElseThrow(()-> new AssertionFailure("Booking should exist in DB"));
            assertThat(bookingUpdated.getId()).isEqualTo(idToUpdate);
            assertThat(bookingUpdated.getStatus().name()).isEqualTo(updateStatusDTO.getStatus());
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            String idToUpdate = bookings.getFirst().getId();

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .patch("/api/bookings/" + idToUpdate + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateStatusDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            Booking bookingUpdated = bookingRepository.findById(idToUpdate).orElseThrow(()-> new AssertionFailure("Booking should exist in DB"));
            assertThat(bookingUpdated.getId()).isEqualTo(idToUpdate);
            assertThat(bookingUpdated.getStatus().name()).isEqualTo(bookings.getFirst().getStatus().name());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idToUpdate = "abcd1234";
            updateStatusDTO.setStatus(BookingStatus.CANCELLED.name());

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .patch("/api/bookings/" + idToUpdate + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateStatusDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
        }

        @Test
        void errorWhenNotFoundBookingById() throws Exception {
            String idToUpdate = UUID.randomUUID().toString();
            updateStatusDTO.setStatus(BookingStatus.CANCELLED.name());

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .patch("/api/bookings/" + idToUpdate + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateStatusDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found booking with id")));
        }
    }

    @Nested
    class Review {

        CreateReviewDTO createReviewDTO;

        @BeforeEach
        void setup(){
            createReviewDTO = new CreateReviewDTO();
        }

        @Test
        void review() throws Exception {
            String idToReview = bookings.getFirst().getId();
            createReviewDTO.setRating(3);
            createReviewDTO.setReview("test review");

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings/" + idToReview + "/review")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createReviewDTO))
                                    .header("X-UserId", bookings.getFirst().getUserId())

                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToReview))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.rating").value(createReviewDTO.getRating()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.review").value(createReviewDTO.getReview()));

            Booking bookingUpdated = bookingRepository.findById(idToReview).orElseThrow(()-> new AssertionFailure("Booking should exist in DB"));
            assertThat(bookingUpdated.getId()).isEqualTo(idToReview);
            assertThat(bookingUpdated.getReview()).isEqualTo(createReviewDTO.getReview());
            assertThat(bookingUpdated.getRating()).isEqualTo(createReviewDTO.getRating());
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            String idToReview = bookings.getFirst().getId();

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings/" + idToReview + "/review")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createReviewDTO))
                                    .header("X-UserId", bookings.getFirst().getUserId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            Booking bookingUpdated = bookingRepository.findById(idToReview).orElseThrow(()-> new AssertionFailure("Booking should exist in DB"));
            assertThat(bookingUpdated.getId()).isEqualTo(idToReview);
            assertThat(bookingUpdated.getReview()).isNull();
            assertThat(bookingUpdated.getRating()).isNull();
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idToReview = "abcd1234";
            createReviewDTO.setRating(3);
            createReviewDTO.setReview("test review");

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings/" + idToReview + "/review")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createReviewDTO))
                                    .header("X-UserId", bookings.getFirst().getUserId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
        }

        @Test
        void errorWhenNotFoundBookingById() throws Exception {
            String idToReview = UUID.randomUUID().toString();
            createReviewDTO.setRating(3);
            createReviewDTO.setReview("test review");

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/bookings/" + idToReview + "/review")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createReviewDTO))
                                    .header("X-UserId", bookings.getFirst().getUserId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found booking with id")));
        }
    }
}
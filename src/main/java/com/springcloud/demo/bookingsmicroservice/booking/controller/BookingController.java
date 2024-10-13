package com.springcloud.demo.bookingsmicroservice.booking.controller;

import com.springcloud.demo.bookingsmicroservice.booking.dto.*;
import com.springcloud.demo.bookingsmicroservice.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseBookingDTO create(@Valid @RequestBody CreateBookingDTO createBookingDTO, @RequestHeader("X-UserId") String idUserLogged) {
        return bookingService.create(createBookingDTO, idUserLogged);
    }

    @GetMapping
    List<ResponseBookingDTO> findAll(@Valid @ModelAttribute FilterBookingDTO filters) {
        return bookingService.findAll(filters);
    }

    @GetMapping("/{id}")
    ResponseBookingDTO findById(@PathVariable @UUID String id) {
        return bookingService.findById(id);
    }

    @PatchMapping("/{id}/status")
    ResponseBookingDTO updateStatus(
            @PathVariable @UUID String id,
            @RequestBody @Valid UpdateStatusDTO updateStatusDTO,
            @RequestHeader("X-UserId") String idUserLogged
    ) {
        return bookingService.updateStatus(id, updateStatusDTO, idUserLogged);
    }

    @PostMapping("/{id}/review")
    ResponseBookingDTO review(
            @PathVariable @UUID String id,
            @Valid @RequestBody CreateReviewDTO createReviewDTO,
            @RequestHeader("X-UserId") String idUserLogged
    ) {
        return bookingService.review(id, createReviewDTO, idUserLogged);
    }
}

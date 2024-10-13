package com.springcloud.demo.bookingsmicroservice.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDTO {

    @Future
    @NotNull
    private LocalDateTime checkIn;

    @Future
    @NotNull
    private LocalDateTime checkOut;

    @UUID
    @NotBlank
    private String roomId;
}

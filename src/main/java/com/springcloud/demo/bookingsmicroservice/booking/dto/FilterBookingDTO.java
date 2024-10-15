package com.springcloud.demo.bookingsmicroservice.booking.dto;

import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterBookingDTO {
    @UUID
    private String userId;

    @UUID
    private String roomId;

    private OffsetDateTime checkIn;

    private OffsetDateTime checkOut;

    private BookingStatus status;

    @Positive
    @Max(10)
    private Integer minRating;

    @Positive
    @Max(10)
    private Integer maxRating;

    @PositiveOrZero
    @Builder.Default
    private Integer page = 1;

    @PositiveOrZero
    @Builder.Default
    private Integer limit = 20;
}

package com.springcloud.demo.bookingsmicroservice.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDTO {

    @Positive
    @Max(10)
    @NotNull
    private Integer rating;

    @Length(min = 3, max = 255)
    private String review;
}

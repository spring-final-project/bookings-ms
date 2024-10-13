package com.springcloud.demo.bookingsmicroservice.exceptions.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    String message;
    int status;

    @Builder.Default
    List<String> errors = new ArrayList<>();
}

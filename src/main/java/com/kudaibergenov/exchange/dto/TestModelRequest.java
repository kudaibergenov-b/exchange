package com.kudaibergenov.exchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TestModelRequest {

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Min(value = 1, message = "Days must be greater than 0")
    private int days;
}

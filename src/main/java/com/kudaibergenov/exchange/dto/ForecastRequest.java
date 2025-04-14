package com.kudaibergenov.exchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ForecastRequest {

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Min(value = 1, message = "Forecast days must be at least 1")
    private int days = 7; // default to 7 if not provided
}

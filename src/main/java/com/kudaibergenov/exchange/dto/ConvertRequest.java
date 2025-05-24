package com.kudaibergenov.exchange.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertRequest {

    @NotBlank(message = "Source currency must not be blank")
    private String from;

    @NotBlank(message = "Target currency must not be blank")
    private String to;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    private Double customFromRate;

    private Double customToRate;
}
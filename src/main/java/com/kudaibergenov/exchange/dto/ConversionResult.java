package com.kudaibergenov.exchange.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversionResult {

    private String from;
    private String to;
    private double amount;
    private double fromRate;
    private double toRate;
    private double convertedAmount;

    public ConversionResult(String from, String to, double amount, double fromRate, double toRate, double convertedAmount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.fromRate = fromRate;
        this.toRate = toRate;
        this.convertedAmount = convertedAmount;
    }

}
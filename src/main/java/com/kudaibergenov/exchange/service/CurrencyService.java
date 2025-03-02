package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.CurrencyXmlParser;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;

@Service
public class CurrencyService {

    private final CurrencyRateRepository repository;

    public CurrencyService(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    public void fetchAndSaveHistoricalRates(LocalDate startDate, LocalDate endDate) {
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            System.out.println("Fetching rates for: " + date);
            Map<String, Double> rates = CurrencyXmlParser.fetchRates(date.toString());

            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                CurrencyRate currencyRate = new CurrencyRate();
                currencyRate.setDate(date);
                currencyRate.setCurrencyCode(entry.getKey());
                currencyRate.setRate(entry.getValue());

                repository.save(currencyRate);
            }
            date = date.plusDays(1); // Переход на следующий день
        }
        System.out.println("Historical rates saved successfully!");
    }
}
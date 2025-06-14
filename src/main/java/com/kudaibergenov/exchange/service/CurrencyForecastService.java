package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.dto.ForecastResponse;
import com.kudaibergenov.exchange.dto.TestModelResponse;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ArimaModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyForecastService {

    private final CurrencyRateRepository repository;

    public CurrencyForecastService(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    public ForecastResponse forecast(String currency, LocalDate startDate, int days) {
        validateDays(days);

        LocalDate trainingStartDate = startDate.minusYears(2);
        LocalDate lastTrainingDate = startDate.minusDays(1);

        List<BigDecimal> historyRates = getHistoryRates(currency, trainingStartDate, lastTrainingDate);
        if (historyRates.size() < 100) {
            throw new IllegalStateException("Недостаточно данных для прогнозирования.");
        }

        int[] bestParams = ArimaModel.findBestParams(historyRates);
        BigDecimal[] predictedRates = ArimaModel.predict(historyRates, days, bestParams[0], bestParams[1], bestParams[2]);

        LocalDate endDate = startDate.plusDays(days - 1);

        return new ForecastResponse(
                currency,
                startDate,
                endDate,
                Arrays.asList(predictedRates),
                historyRates
        );
    }

    public TestModelResponse testModel(String currency, LocalDate startDate, int days) {
        LocalDate endDate = startDate.plusDays(days - 1);
        LocalDate lastTrainingDate = startDate.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2);

        List<CurrencyRate> actualRates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);
        if (actualRates.isEmpty()) {
            throw new IllegalStateException("Нет данных за " + startDate + " - " + endDate + " для " + currency);
        }

        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);
        if (trainingData.size() < 100) {
            throw new IllegalStateException("Недостаточно данных для обучения модели.");
        }

        List<BigDecimal> trainingRates = trainingData.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .map(CurrencyRate::getRate)
                .toList();

        int[] bestParams = ArimaModel.findBestParams(trainingRates);
        BigDecimal[] predictedRates = ArimaModel.predict(trainingRates, actualRates.size(), bestParams[0], bestParams[1], bestParams[2]);
        BigDecimal mae = calculateMAE(actualRates, predictedRates);

        log.info("Тестирование модели ARIMA для {} за период: {} - {}", currency, startDate, endDate);
        for (int i = 0; i < actualRates.size(); i++) {
            log.info("Дата: {} | Факт: {} | Прогноз: {}", actualRates.get(i).getDate(), actualRates.get(i).getRate(), predictedRates[i]);
        }
        log.info("Средняя ошибка {}", mae);

        return new TestModelResponse(currency, startDate, endDate, actualRates, predictedRates, mae);
    }

    public List<BigDecimal> getHistoryRates(String currency, LocalDate start, LocalDate end) {
        return repository.findByDateBetweenAndCurrencyCode(start, end, currency).stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateMAE(List<CurrencyRate> actualRates, BigDecimal[] predictedRates) {
        BigDecimal mae = BigDecimal.ZERO;
        for (int i = 0; i < actualRates.size(); i++) {
            mae = mae.add(actualRates.get(i).getRate().subtract(predictedRates[i]).abs());
        }
        return mae.divide(BigDecimal.valueOf(actualRates.size()), RoundingMode.HALF_UP);
    }

    private void validateDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Прогнозируемый период должен быть больше 0");
        }
    }
}
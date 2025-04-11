package com.kudaibergenov.exchange.service;

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

    public List<BigDecimal> forecast(String currency, LocalDate startDate, int days) {
        validateDays(days);

        List<BigDecimal> trainingRates = getTrainingRates(currency, startDate);
        return Arrays.asList(ArimaModel.predict(trainingRates, days, 1, 1, 0));
    }

    public List<BigDecimal> forecastForWeek(String currency, LocalDate startDate) {
        return forecast(currency, startDate, 7);
    }

    public Map<String, Object> testModelForWeek(String currency, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        List<CurrencyRate> actualRates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);
        if (actualRates.isEmpty()) {
            throw new IllegalStateException("Нет данных за " + startDate + " - " + endDate + " для " + currency);
        }

        List<BigDecimal> trainingRates = getTrainingRates(currency, startDate);
        BigDecimal[] predictedRates = ArimaModel.predict(trainingRates, actualRates.size(), 1, 1, 0);

        BigDecimal mae = calculateMAE(actualRates, predictedRates);

        log.info("Тестирование модели ARIMA(1,1,0) для {} за неделю: {} - {}", currency, startDate, endDate);
        for (int i = 0; i < actualRates.size(); i++) {
            log.info("Дата: {} | Фактический: {} | Прогноз: {}", actualRates.get(i).getDate(), actualRates.get(i).getRate(), predictedRates[i]);
        }
        log.info("Средняя абсолютная ошибка (MAE): {}", mae);

        return Map.of(
                "currency", currency,
                "start_date", startDate,
                "end_date", endDate,
                "actual_rates", actualRates,
                "predicted_rates", predictedRates,
                "mae", mae
        );
    }

    private List<BigDecimal> getTrainingRates(String currency, LocalDate forecastStartDate) {
        LocalDate lastTrainingDate = forecastStartDate.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2);

        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);
        if (trainingData.size() < 100) {
            throw new IllegalStateException("Недостаточно данных для прогнозирования.");
        }

        return trainingData.stream()
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
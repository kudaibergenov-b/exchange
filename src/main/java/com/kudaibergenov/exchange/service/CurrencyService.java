package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ArimaModel;
import com.kudaibergenov.exchange.util.ExcelImporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyService {

    private static final Logger logger = Logger.getLogger(CurrencyService.class.getName());

    private final ExcelImporter excelImporter;
    private final CurrencyRateRepository repository;

    public CurrencyService(ExcelImporter excelImporter, CurrencyRateRepository repository) {
        this.excelImporter = excelImporter;
        this.repository = repository;
    }

    public void importFromExcel(String filePath) {
        try {
            excelImporter.importData(filePath);
            logger.info("Импорт завершен успешно!");
        } catch (Exception e) {
            logger.severe("Ошибка при импорте файла: " + e.getMessage());
        }
    }

    public List<CurrencyRate> getHistoricalRates(String currency, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Количество дней должно быть положительным");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<CurrencyRate> rates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);

        if (rates.isEmpty()) {
            logger.warning("Нет данных за последние " + days + " дней для валюты: " + currency);
        }

        return rates;
    }

    public List<BigDecimal> forecastForWeek(String currency, int year, int month, int startDay) {
        LocalDate startOfWeek = LocalDate.of(year, month, startDay);
        LocalDate lastTrainingDate = startOfWeek.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2); // 2 года данных

        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);
        if (trainingData.size() < 100) {
            throw new IllegalStateException("Недостаточно данных для прогнозирования.");
        }

        List<BigDecimal> trainingRates = trainingData.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        return Arrays.asList(ArimaModel.predict(trainingRates, 7));
    }

    public Map<String, Object> testModelForWeek(String currency, int year, int month, int startDay) {
        LocalDate startOfWeek = LocalDate.of(year, month, startDay);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDate lastTrainingDate = startOfWeek.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2);

        List<CurrencyRate> actualRates = repository.findByDateBetweenAndCurrencyCode(startOfWeek, endOfWeek, currency);
        if (actualRates.isEmpty()) {
            throw new IllegalStateException("Нет данных за " + startOfWeek + " - " + endOfWeek + " для " + currency);
        }

        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);
        if (trainingData.size() < 100) {
            throw new IllegalStateException("Недостаточно данных для обучения модели.");
        }

        List<BigDecimal> trainingRates = trainingData.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        BigDecimal[] predictedRates = ArimaModel.predict(trainingRates, actualRates.size(), 1, 1, 0);

        BigDecimal totalError = BigDecimal.ZERO;
        for (int i = 0; i < actualRates.size(); i++) {
            totalError = totalError.add(actualRates.get(i).getRate().subtract(predictedRates[i]).abs());
        }
        BigDecimal mae = totalError.divide(BigDecimal.valueOf(actualRates.size()), RoundingMode.HALF_UP);

        log.info("Тестирование модели с ARIMA(1,1,0) для {} за неделю: {} - {}", currency, startOfWeek, endOfWeek);
        for (int i = 0; i < actualRates.size(); i++) {
            log.info("Дата: {} | Фактический: {} | Прогнозируемый: {}", actualRates.get(i).getDate(), actualRates.get(i).getRate(), predictedRates[i]);
        }
        log.info("Средняя абсолютная ошибка (MAE): {}", mae);

        Map<String, Object> result = new HashMap<>();
        result.put("currency", currency);
        result.put("start_date", startOfWeek);
        result.put("end_date", endOfWeek);
        result.put("actual_rates", actualRates);
        result.put("predicted_rates", predictedRates);
        result.put("mae", mae);
        return result;
    }
}

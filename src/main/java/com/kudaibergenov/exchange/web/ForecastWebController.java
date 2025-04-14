package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.dto.ForecastRequest;
import com.kudaibergenov.exchange.dto.ForecastResponse;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
import com.kudaibergenov.exchange.service.FxKgService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

@Controller
public class ForecastWebController {

    private final CurrencyForecastService forecastService;
    private final FxKgService fxKgService;

    public ForecastWebController(CurrencyForecastService forecastService, FxKgService fxKgService) {
        this.forecastService = forecastService;
        this.fxKgService = fxKgService;
    }

    @GetMapping("/forecast")
    public String showForecastForm(Model model) {
        model.addAttribute("request", new ForecastRequest());
        model.addAttribute("currencies", getAvailableCurrencies());
        return "forecast";
    }

    @PostMapping("/forecast")
    public String handleForecast(@ModelAttribute("request") @Valid ForecastRequest request, Model model) {
        ForecastResponse response = forecastService.forecast(
                request.getCurrency(),
                request.getStartDate(),
                request.getDays()
        );

        // Генерация дат для прогноза
        List<String> forecastDates = IntStream.range(0, request.getDays())
                .mapToObj(i -> request.getStartDate().plusDays(i).toString())
                .toList();

        // Генерация фиктивных дат для исторических данных
        LocalDate historyStart = request.getStartDate().minusYears(2);
        List<String> historyDates = IntStream.range(0, response.getHistoryRates().size())
                .mapToObj(i -> historyStart.plusDays(i).toString())
                .toList();

        model.addAttribute("currencies", getAvailableCurrencies());
        model.addAttribute("currency", response.getCurrency());
        model.addAttribute("start", response.getStartDate());
        model.addAttribute("end", response.getEndDate());

        model.addAttribute("predictedRates", response.getPredictedRates());
        model.addAttribute("predictedLabels", forecastDates);

        model.addAttribute("historyRates", response.getHistoryRates());
        model.addAttribute("historyLabels", historyDates);

        return "forecast";
    }

    private List<String> getAvailableCurrencies() {
        try {
            JsonNode node = fxKgService.getCentralBankRates();
            List<String> codes = new ArrayList<>();
            node.fieldNames().forEachRemaining(code -> {
                if (!List.of("id", "created_at", "updated_at", "is_current").contains(code)) {
                    codes.add(code.toUpperCase());
                }
            });
            Collections.sort(codes);
            return codes;
        } catch (Exception e) {
            return List.of("USD", "EUR", "RUB");
        }
    }
}
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

import java.math.BigDecimal;
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

        List<BigDecimal> fullHistory = response.getHistoryRates();

        // Ограничим только последние 30 дней истории (или меньше, если данных недостаточно)
        int historyLimit = 30;
        List<BigDecimal> recentHistory = fullHistory.size() > historyLimit
                ? fullHistory.subList(fullHistory.size() - historyLimit, fullHistory.size())
                : fullHistory;

        LocalDate historyStart = request.getStartDate().minusDays(recentHistory.size());
        List<String> historyLabels = IntStream.range(0, recentHistory.size())
                .mapToObj(i -> historyStart.plusDays(i).toString())
                .toList();

        List<String> forecastLabels = IntStream.range(0, request.getDays())
                .mapToObj(i -> request.getStartDate().plusDays(i).toString())
                .toList();

        model.addAttribute("currencies", getAvailableCurrencies());
        model.addAttribute("currency", response.getCurrency());
        model.addAttribute("start", response.getStartDate());
        model.addAttribute("end", response.getEndDate());

        model.addAttribute("predictedRates", response.getPredictedRates());
        model.addAttribute("predictedLabels", forecastLabels);

        model.addAttribute("historyRates", recentHistory);
        model.addAttribute("historyLabels", historyLabels);

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
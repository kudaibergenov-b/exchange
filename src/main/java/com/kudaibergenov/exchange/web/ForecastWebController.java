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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ForecastWebController {

    private final CurrencyForecastService forecastService;
    private final FxKgService fxKgService;

    public ForecastWebController(CurrencyForecastService forecastService, FxKgService fxKgService) {
        this.forecastService = forecastService;
        this.fxKgService = fxKgService;
    }

    @GetMapping("/forecast")
    public String forecastForm(Model model) {
        model.addAttribute("currencies", getAvailableCurrencies());
        model.addAttribute("request", new ForecastRequest());
        return "forecast";
    }

    @PostMapping("/forecast")
    public String predictForecast(@ModelAttribute @Valid ForecastRequest request, Model model) {
        ForecastResponse response = forecastService.forecast(request.getCurrency(), request.getStartDate(), request.getDays());

        List<String> dates = new ArrayList<>();
        for (int i = 0; i < response.getPredictedRates().size(); i++) {
            dates.add(response.getStartDate().plusDays(i).toString());
        }

        model.addAttribute("request", request); // ✅ Добавь это!
        model.addAttribute("currencies", getAvailableCurrencies());
        model.addAttribute("currency", response.getCurrency());
        model.addAttribute("start", response.getStartDate());
        model.addAttribute("end", response.getEndDate());
        model.addAttribute("rates", response.getPredictedRates());
        model.addAttribute("dates", dates);
        model.addAttribute("values", response.getPredictedRates());

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
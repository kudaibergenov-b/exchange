package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class CurrencyWebController {

    private final FxKgService fxKgService;

    public CurrencyWebController(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    @GetMapping("/currency/current")
    public String showCurrentRates(Model model) {
        JsonNode rates = fxKgService.getCentralBankRates();
        Map<String, String> currencyMap = new TreeMap<>();

        rates.fieldNames().forEachRemaining(code -> {
            if (!List.of("id", "created_at", "updated_at", "is_current").contains(code)) {
                currencyMap.put(code.toUpperCase(), rates.get(code).asText());
            }
        });

        model.addAttribute("rates", currencyMap);
        return "current";
    }
}
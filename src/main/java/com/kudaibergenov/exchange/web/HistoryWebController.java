package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

@Controller
public class HistoryWebController {

    private final CurrencyDataService currencyDataService;
    private final FxKgService fxKgService;

    public HistoryWebController(CurrencyDataService currencyDataService, FxKgService fxKgService) {
        this.currencyDataService = currencyDataService;
        this.fxKgService = fxKgService;
    }

    @GetMapping("/history")
    public String showHistory(
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        // получаем список всех валют (для выпадающего списка)
        List<String> currencyList = getAvailableCurrencyList();
        model.addAttribute("currencies", currencyList);

        // если значения не переданы, устанавливаем дефолтные
        if (start == null) start = LocalDate.now().minusDays(7);
        if (end == null) end = LocalDate.now();
        if (currency == null) currency = "USD";

        // Создаем финальную переменную для использования внутри лямбда-выражения
        final String selectedCurrency = currency;

        // Фильтрация по валюте
        List<CurrencyRate> rates = currencyDataService.getHistory(start, end).stream()
                .filter(rate -> rate.getCurrencyCode().equalsIgnoreCase(selectedCurrency)) // фильтруем по выбранной валюте
                .toList();

        model.addAttribute("rates", rates);
        model.addAttribute("currency", selectedCurrency);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "history";
    }

    private List<String> getAvailableCurrencyList() {
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

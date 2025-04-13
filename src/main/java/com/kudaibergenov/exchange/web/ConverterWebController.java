package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
public class ConverterWebController {

    private final CurrencyConverterService converterService;
    private final FxKgService fxKgService;

    public ConverterWebController(CurrencyConverterService converterService, FxKgService fxKgService) {
        this.converterService = converterService;
        this.fxKgService = fxKgService;
    }

    @GetMapping("/converter")
    public String showConverterForm(Model model) {
        List<String> currencies = getCurrencyCodes();
        model.addAttribute("currencies", currencies);
        model.addAttribute("averageRates", fxKgService.getAverageRates());
        model.addAttribute("bestRates", fxKgService.getBestRates());
        return "converter";
    }

    @PostMapping("/converter")
    public String convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount,
            Model model
    ) {
        var result = converterService.convert(from, to, amount);
        var reversed = result.getToRate() / result.getFromRate();
        List<String> currencies = getCurrencyCodes();

        model.addAttribute("currencies", currencies);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("amount", amount);
        model.addAttribute("result", result);
        model.addAttribute("reversed", round(reversed));
        model.addAttribute("averageRates", fxKgService.getAverageRates());
        model.addAttribute("bestRates", fxKgService.getBestRates());

        return "converter";
    }

    private List<String> getCurrencyCodes() {
        JsonNode centralRates = fxKgService.getCentralBankRates();
        List<String> codes = new ArrayList<>();
        centralRates.fieldNames().forEachRemaining(code -> {
            if (!List.of("id", "created_at", "updated_at", "is_current").contains(code)) {
                codes.add(code.toUpperCase());
            }
        });
        Collections.sort(codes);
        return codes;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}

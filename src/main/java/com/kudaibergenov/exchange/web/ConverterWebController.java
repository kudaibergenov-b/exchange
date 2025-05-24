package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.dto.ConversionResult;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
import com.kudaibergenov.exchange.service.FxKgService;
import lombok.Getter;
import lombok.Setter;
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
    public String showConverterPage(Model model) {
        List<String> currencies = getCurrencyCodes();
        Map<String, RateDto> averageRates = parseRates(fxKgService.getAverageRates());
        Map<String, RateDto> bestRates = parseRates(fxKgService.getBestRates());

        model.addAttribute("currencies", currencies);
        model.addAttribute("averageRates", averageRates);
        model.addAttribute("bestRates", bestRates);
        return "converter";
    }

    @PostMapping("/converter")
    public String convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount,
            @RequestParam(required = false) Double customFromRate,
            @RequestParam(required = false) Double customToRate,
            Model model
    ) {
        ConversionResult result = converterService.convert(from, to, amount, customFromRate, customToRate);
        double reversed = result.getToRate() / result.getFromRate();

        List<String> currencies = getCurrencyCodes();
        Map<String, RateDto> averageRates = parseRates(fxKgService.getAverageRates());
        Map<String, RateDto> bestRates = parseRates(fxKgService.getBestRates());

        model.addAttribute("currencies", currencies);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("amount", amount);
        model.addAttribute("customFromRate", customFromRate);
        model.addAttribute("customToRate", customToRate);
        model.addAttribute("customRateCheck", customFromRate != null && customToRate != null);
        model.addAttribute("result", result);
        model.addAttribute("reversed", round(reversed));
        model.addAttribute("averageRates", averageRates);
        model.addAttribute("bestRates", bestRates);

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

    private Map<String, RateDto> parseRates(JsonNode dataNode) {
        Map<String, RateDto> map = new TreeMap<>();

        if (dataNode == null || !dataNode.isObject()) return map;

        Iterator<Map.Entry<String, JsonNode>> fields = dataNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();

            if (List.of("id", "updated_at", "type", "created_at", "slug", "title").contains(key)) continue;
            if (!key.contains("_")) continue;

            String[] parts = key.split("_");
            if (parts.length != 2) continue;

            String action = parts[0]; // "buy" or "sell"
            String currency = parts[1].toUpperCase();

            RateDto dto = map.getOrDefault(currency, new RateDto());
            double value = entry.getValue().asDouble();

            if (action.equals("buy")) dto.setBuy(value);
            else if (action.equals("sell")) dto.setSell(value);

            map.put(currency, dto);
        }

        return map;
    }

    @Getter
    @Setter
    public static class RateDto {
        private double buy;
        private double sell;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}

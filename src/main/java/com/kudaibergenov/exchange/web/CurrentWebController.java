package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/current")
public class CurrentWebController {

    private final FxKgService fxKgService;

    public CurrentWebController(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    @GetMapping("/nbkr")
    public String showNbkrRates(Model model) {
        JsonNode centralRates = fxKgService.getCentralBankRates();
        model.addAttribute("rates", centralRates);
        return "current-nbkr";
    }

    @GetMapping("/banks")
    public String showBankRates(Model model) {
        JsonNode currentRates = fxKgService.getCurrentRates();
        List<Map<String, Object>> banks = new ArrayList<>();

        currentRates.forEach(bankNode -> {
            String title = bankNode.get("title").asText();
            String slug = bankNode.path("slug").asText(null);
            String website = bankNode.path("website_url").asText(null);
            JsonNode rates = bankNode.get("rates");

            if (rates != null && rates.isArray()) {
                for (JsonNode rateEntry : rates) {
                    if ("regular".equals(rateEntry.path("type").asText())) {
                        Map<String, Object> bank = new HashMap<>();
                        bank.put("title", title);
                        bank.put("slug", slug);
                        bank.put("website", website);

                        Map<String, Map<String, String>> currencies = new LinkedHashMap<>();
                        for (String code : List.of("usd", "eur", "rub")) {
                            String buy = rateEntry.path("buy_" + code).asText(null);
                            String sell = rateEntry.path("sell_" + code).asText(null);
                            if (buy != null && sell != null) {
                                currencies.put(code.toUpperCase(), Map.of(
                                        "buy", buy,
                                        "sell", sell
                                ));
                            }
                        }

                        if (!currencies.isEmpty()) {
                            bank.put("currencies", currencies);
                            banks.add(bank);
                        }
                        break;
                    }
                }
            }
        });

        model.addAttribute("banks", banks);
        return "current-banks";
    }
}
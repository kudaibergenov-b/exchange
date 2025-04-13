package com.kudaibergenov.exchange.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class CurrentWebController {

    private final FxKgService fxKgService;

    public CurrentWebController(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    @GetMapping("/current")
    public String showCurrentRates(Model model) {
        JsonNode centralRates = fxKgService.getCentralBankRates();
        JsonNode currentRates = fxKgService.getCurrentRates();

        List<String> targetBanks = List.of("Элдик банк", "КИКБ", "Оптима Банк", "КБ КЫРГЫЗСТАН");

        List<Map<String, Object>> banks = new ArrayList<>();

        currentRates.forEach(bankNode -> {
            String title = bankNode.get("title").asText();
            if (targetBanks.contains(title)) {
                JsonNode rates = bankNode.get("rates");
                if (rates != null && rates.isArray()) {
                    for (JsonNode rateEntry : rates) {
                        if ("regular".equals(rateEntry.get("type").asText())) {
                            Map<String, Object> bank = new HashMap<>();
                            bank.put("title", title);
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

                            bank.put("currencies", currencies);
                            banks.add(bank);
                            break; // только один "regular"
                        }
                    }
                }
            }
        });

        model.addAttribute("rates", centralRates);
        model.addAttribute("banks", banks);
        return "current";
    }
}
package com.kudaibergenov.exchange.web;

import com.kudaibergenov.exchange.dto.ForecastRequest;
import com.kudaibergenov.exchange.dto.ForecastResponse;
import com.kudaibergenov.exchange.dto.TestModelResponse;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
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

    public ForecastWebController(CurrencyForecastService forecastService) {
        this.forecastService = forecastService;
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

    @GetMapping("/forecast/test")
    public String showTestForm(Model model) {
        model.addAttribute("request", new ForecastRequest());
        model.addAttribute("currencies", getAvailableCurrencies());
        return "forecast-test";
    }

    @PostMapping("/forecast/test")
    public String handleForecastTest(@ModelAttribute("request") @Valid ForecastRequest request, Model model) {
        TestModelResponse response = forecastService.testModel(
                request.getCurrency(),
                request.getStartDate(),
                request.getDays()
        );

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < response.getActualRates().size(); i++) {
            labels.add(response.getStartDate().plusDays(i).toString());
        }

        model.addAttribute("currencies", getAvailableCurrencies());
        model.addAttribute("request", request);
        model.addAttribute("currency", response.getCurrency());
        model.addAttribute("start", response.getStartDate());
        model.addAttribute("end", response.getEndDate());
        model.addAttribute("labels", labels);

        model.addAttribute("actualRates", response.getActualRates().stream()
                .map(CurrencyRate::getRate).toList());
        model.addAttribute("predictedRates", Arrays.asList(response.getPredictedRates()));
        model.addAttribute("mae", response.getMae());

        return "forecast-test";
    }

    private List<String> getAvailableCurrencies() {
        return List.of("USD", "EUR", "RUB", "KZT");
    }
}
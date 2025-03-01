package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CurrencyService {

    private final CurrencyRateRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fxkg.api.url}")
    private String apiUrl;

    @Value("${fxkg.api.token}")
    private String apiToken;


    public CurrencyService(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    public void fetchAndSaveRates() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> rates = response.getBody();

        if (rates != null) {
            CurrencyRate currencyRate = new CurrencyRate(
                    LocalDateTime.now(),
                    Double.parseDouble(rates.get("usd").toString()),
                    Double.parseDouble(rates.get("eur").toString()),
                    Double.parseDouble(rates.get("rub").toString()),
                    Double.parseDouble(rates.get("kzt").toString())
            );

            repository.save(currencyRate);
            System.out.println("Saved new currency rates: " + currencyRate);
        }
    }
}

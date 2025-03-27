package com.kudaibergenov.exchange.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class FxKgService {

    @Value("${fxkg.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://data.fx.kg/api/v1";

    private String sendRequest(String endpoint) {
        String url = BASE_URL + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        return restTemplate.exchange(request, String.class).getBody();
    }

    public String getAverageRates() {
        return sendRequest("/average");
    }

    public String getBestRates() {
        return sendRequest("/best");
    }

    public String getCurrentRates() {
        return sendRequest("/current");
    }

    public String getCentralBankRates() {
        return sendRequest("/central");
    }
}
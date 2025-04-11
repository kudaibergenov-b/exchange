package com.kudaibergenov.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "https://data.fx.kg/api/v1";

    private JsonNode sendRequest(String endpoint) {
        try {
            String url = BASE_URL + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
            String response = restTemplate.exchange(request, String.class).getBody();
            return objectMapper.readTree(response); // ✅ превращаем String в JSON
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving data from FX.KG", e);
        }
    }

    public JsonNode getAverageRates() {
        return sendRequest("/average");
    }

    public JsonNode getBestRates() {
        return sendRequest("/best");
    }

    public JsonNode getCurrentRates() {
        return sendRequest("/current");
    }

    public JsonNode getCentralBankRates() {
        return sendRequest("/central");
    }
}

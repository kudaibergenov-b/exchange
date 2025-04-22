package com.kudaibergenov.exchange.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.ConverterController;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConverterControllerPerformanceTest {

    private MockMvc mockMvc;

    @Mock
    private FxKgService fxKgService;

    @InjectMocks
    private CurrencyConverterService converterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ConverterController converterController = new ConverterController(converterService);
        mockMvc = MockMvcBuilders.standaloneSetup(converterController).build();
    }

    @Test
    void performanceTest_convert_shouldHandleMultipleSequentialRequestsQuickly() throws Exception {
        ObjectNode mockRates = objectMapper.createObjectNode();
        mockRates.put("usd", 87.5);
        mockRates.put("eur", 99.3);

        when(fxKgService.getCentralBankRates()).thenReturn(mockRates);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("from", "USD");
        requestBody.put("to", "EUR");
        requestBody.put("amount", 100.0);

        int iterations = 100;
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            mockMvc.perform(post("/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody.toString()))
                    .andExpect(status().isOk());
        }

        long duration = System.currentTimeMillis() - start;
        double avgTime = duration / (double) iterations;

        System.out.println("Total time: " + duration + "ms for " + iterations + " requests.");
        System.out.println("Average response time: " + avgTime + "ms");

        // Можно поставить условие, если хочешь строгое ограничение
        assert avgTime < 100 : "Среднее время отклика слишком высокое!";
    }
}
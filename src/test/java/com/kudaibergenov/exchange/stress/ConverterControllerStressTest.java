package com.kudaibergenov.exchange.stress;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConverterControllerStressTest {

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
    void stressTest_convert_shouldHandleConcurrentRequests() throws Exception {
        ObjectNode mockRates = objectMapper.createObjectNode();
        mockRates.put("usd", 87.5);
        mockRates.put("eur", 99.3);

        when(fxKgService.getCentralBankRates()).thenReturn(mockRates);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("from", "USD");
        requestBody.put("to", "EUR");
        requestBody.put("amount", 100.0);

        int threadCount = 20;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    mockMvc.perform(post("/convert")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody.toString()))
                            .andExpect(status().isOk());
                }
                return null;
            }));
        }

        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }

        executor.shutdown();
        System.out.println("Stress test passed with " + (threadCount * requestPerThread) + " concurrent requests.");
    }
}
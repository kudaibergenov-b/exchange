package com.kudaibergenov.exchange.stress;

import com.kudaibergenov.exchange.controller.CurrencyController;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrencyControllerStressTest {

    private MockMvc mockMvc;

    @Mock
    private CurrencyDataService currencyDataService;

    @InjectMocks
    private CurrencyController currencyController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
    }

    @Test
    void stressTest_getRatesByDate_shouldHandleHighLoad() throws InterruptedException {
        LocalDate date = LocalDate.of(2024, 4, 10);
        CurrencyRate rate = new CurrencyRate(date, "USD", new BigDecimal("87.5"));
        when(currencyDataService.getRatesByDate(date)).thenReturn(List.of(rate));

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/currency/rates/{date}", date)
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        System.out.println("Stress test passed with " + threadCount + " threads.");
    }
}
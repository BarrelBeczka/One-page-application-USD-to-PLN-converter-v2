package com.barrelbeczka.currency.controller;

import com.barrelbeczka.currency.model.ConversionHistory;
import com.barrelbeczka.currency.repository.ConversionHistoryRepository;
import com.barrelbeczka.currency.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Allow React app
@RequiredArgsConstructor
public class ConversionController {

    private final ExchangeRateService exchangeRateService;
    private final ConversionHistoryRepository historyRepository;

    @GetMapping("/rates")
    public ExchangeRatesResponse getRates() {
        return new ExchangeRatesResponse(
            exchangeRateService.getUsdToPlnRate(),
            exchangeRateService.getPlnToUsdRate()
        );
    }

    @PostMapping("/convert")
    public ConversionHistory convert(@RequestBody ConversionRequest request) {
        Double rate = 0.0;
        Double result = 0.0;

        if ("USD".equalsIgnoreCase(request.sourceCurrency()) && "PLN".equalsIgnoreCase(request.targetCurrency())) {
            rate = exchangeRateService.getUsdToPlnRate();
            result = request.amount() * rate;
        } else if ("PLN".equalsIgnoreCase(request.sourceCurrency()) && "USD".equalsIgnoreCase(request.targetCurrency())) {
            rate = exchangeRateService.getPlnToUsdRate();
            result = request.amount() * rate;
        } else {
            throw new IllegalArgumentException("Unsupported currency pair");
        }

        ConversionHistory history = new ConversionHistory(
                null,
                request.sourceCurrency().toUpperCase(),
                request.targetCurrency().toUpperCase(),
                request.amount(),
                result,
                rate,
                LocalDateTime.now()
        );

        return historyRepository.save(history);
    }

    @GetMapping("/history")
    public List<ConversionHistory> getHistory(@RequestParam(required = false) String sort) {
        List<ConversionHistory> all = historyRepository.findAllByOrderByTimestampDesc();
        
        if ("highest".equalsIgnoreCase(sort)) {
             return all.stream()
                     .sorted(Comparator.comparing(ConversionHistory::getResult).reversed())
                     .collect(Collectors.toList());
        } else if ("lowest".equalsIgnoreCase(sort)) {
            return all.stream()
                    .sorted(Comparator.comparing(ConversionHistory::getResult))
                    .collect(Collectors.toList());
        }
        
        return all;
    }

    public record ExchangeRatesResponse(Double usdToPln, Double plnToUsd) {}
    public record ConversionRequest(String sourceCurrency, String targetCurrency, Double amount) {}
}

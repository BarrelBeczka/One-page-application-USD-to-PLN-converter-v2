package com.barrelbeczka.currency.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import lombok.Getter;

@Service
public class ExchangeRateService {

    @Getter
    private Double plnToUsdRate;

    @Getter
    private Double usdToPlnRate;

    @PostConstruct
    public void init() {
        // Fetch rates here. For now, mocking them or using a simple heuristic.
        // In a real app, use RestTemplate/WebClient to call NBP API.
        // NBP API table A: http://api.nbp.pl/api/exchangerates/rates/a/usd/
        
        System.out.println("Fetching currency rates...");
        // Mocking for robustness if API fails or for simplicity as per MVP
        // Let's assume 1 USD = 3.95 PLN
        this.usdToPlnRate = 3.95; 
        this.plnToUsdRate = 1.0 / this.usdToPlnRate;
        System.out.println("Rates initialized: USD->PLN = " + usdToPlnRate);
    }
    
    // Create a robust fetch implementation if desired later
}

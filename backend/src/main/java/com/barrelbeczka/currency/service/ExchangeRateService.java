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

    private final com.barrelbeczka.currency.repository.CurrencyRateRepository currencyRateRepository;

    public ExchangeRateService(com.barrelbeczka.currency.repository.CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @PostConstruct
    public void init() {
        System.out.println("Fetching currency rates from database...");
        
        com.barrelbeczka.currency.model.CurrencyRate rateEntity = currencyRateRepository.findByCurrencyPair("USD/PLN")
                .orElse(null);

        if (rateEntity != null) {
            this.usdToPlnRate = rateEntity.getRate();
            System.out.println("Found rate in DB: " + this.usdToPlnRate);
        } else {
            System.out.println("Rate not found in DB! Fallback to default.");
            this.usdToPlnRate = 3.95;
        }
        
        this.plnToUsdRate = 1.0 / this.usdToPlnRate;
        System.out.println("Rates initialized: USD->PLN = " + usdToPlnRate);
    }
}

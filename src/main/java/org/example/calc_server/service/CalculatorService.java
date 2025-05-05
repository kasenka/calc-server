package org.example.calc_server.service;

import org.example.calc_server.dto.OhmsLawGetDTO;
import org.example.calc_server.jwt.JwtService;
import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.repository.CurrencyRateRepository;
import org.example.calc_server.repository.HistoryRepository;
import org.example.calc_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CalculatorService {

    private final CurrencyRateRepository currencyRateRepository;
    private final HistoryService historyService;

    public CalculatorService(CurrencyRateRepository currencyRateRepository,
                             HistoryService historyService) {
        this.currencyRateRepository = currencyRateRepository;
        this.historyService = historyService;
    }

    public List<CurrencyRate> getCurrencies() {
        List<CurrencyRate> currencies = currencyRateRepository.findAll();
        return currencies;
    }

    public BigDecimal currencyConvert(long id, double amount,
                                      Authentication authentication) {

        CurrencyRate currencyRate = currencyRateRepository.findById(id).get();

        Double result = amount * currencyRate.getRate();

        BigDecimal roundedResult = BigDecimal.valueOf(result)
                .setScale(3, RoundingMode.HALF_UP);

        historyService.saveCurrencyAction(authentication.getName(),
                currencyRate, amount, roundedResult);

        return roundedResult;

    }

    public String ohms_lawCalculate(OhmsLawGetDTO ohmsLawGetDTO,
                                    Authentication authentication) {
        Double voltage = ohmsLawGetDTO.getVoltage(),
                current = ohmsLawGetDTO.getCurrent(),
                resistance = ohmsLawGetDTO.getResistance();

        String result = "";
        if (voltage == null) result = "voltage = " + current * resistance;
        if (current == null) result = "current = " + voltage / resistance;
        if (resistance == null) result = "resistance = " + voltage / current;

        historyService.saveOhms_lawAction(authentication.getName(),
                voltage, current, resistance, result);
        return result;

    }

}

package org.example.calc_server.service;

import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.model.History;
import org.example.calc_server.repository.CurrencyRateRepository;
import org.example.calc_server.repository.HistoryRepository;
import org.example.calc_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    public void saveCurrencyAction(String username,
                                   CurrencyRate currencyRate, Double amount, BigDecimal result){
        History history = new History();

        history.setAction("From -> To Currency");
        history.setValues(currencyRate.getFromCurrency() + "->" + currencyRate.getToCurrency()
                + "; amount = " + amount);
        history.setResult(String.valueOf(result));
        history.setUsername(username);

        historyRepository.save(history);
    }

    public void saveOhms_lawAction(String username,
                                   Double voltage, Double current, Double resistance,
                                   String result){
        History history = new History();

        history.setAction("ohms-law");
        history.setValues("voltage = " + voltage +
                "; current = " + current +
                "; resistance = " + resistance);
        history.setResult(result);
        history.setUsername(username);

        historyRepository.save(history);
    }

    public List<History> getAllHistory() {
        return historyRepository.findAll();

    }

    public List<History> getUserHistory(Authentication authentication) {
        return historyRepository.findAllByUsername(authentication.getName());
    }
}

package org.example.calc_server.controller;

import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.repository.CurrencyRateRepository;
import org.example.calc_server.repository.HistoryRepository;
import org.example.calc_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/calculator", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> showCalculator() {

    }
}

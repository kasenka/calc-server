package org.example.calc_server.controller;


import jakarta.validation.Valid;
import org.example.calc_server.dto.CurrencyCreateDTO;
import org.example.calc_server.mapper.CurrencyMapper;
import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.CurrencyRateRepository;
import org.example.calc_server.repository.UserRepository;
import org.example.calc_server.service.CalculatorService;
import org.example.calc_server.service.ErrorService;
import org.example.calc_server.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    private CalculatorService calculatorService;

    @Autowired
    private ErrorService errorService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;


    @GetMapping(value = "/createRate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRate(){
        return ResponseEntity.ok()
                .body(Map.of("currencies", calculatorService.getCurrencies()));
    }

    @PostMapping(value = "/createRate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveRate(@RequestBody @Valid CurrencyCreateDTO currencyCreateDTO,
                                        BindingResult bResult) {

        var errors = errorService.checkErrors(bResult);
        if (errors != null){
            return ResponseEntity.badRequest().body(errors);
        }

        CurrencyRate currencyRate = currencyMapper.map(currencyCreateDTO);
        currencyRateRepository.save(currencyRate);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("currency", currencyRate));
    }

    @GetMapping(value = "/usersHistory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> users(){
        return ResponseEntity.ok()
                .body(Map.of("allHistory", historyService.getAllHistory()));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

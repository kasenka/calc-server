
package org.example.calc_server.controller;

import jakarta.validation.Valid;
import org.example.calc_server.dto.CurrencyGetDTO;
import org.example.calc_server.dto.OhmsLawGetDTO;

import org.example.calc_server.service.CalculatorService;
import org.example.calc_server.service.ErrorService;
import org.example.calc_server.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    @Autowired
    private CalculatorService calculatorService;

    @Autowired
    private ErrorService errorService;


    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> showCalculator() {
        return ResponseEntity.ok()
                .body(Map.of("currencies", calculatorService.getCurrencies()));

    }

    @PostMapping(value = "/currency", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> currencyConvert(@RequestBody @Valid CurrencyGetDTO currencyGetDTO,
                                             BindingResult bResult,
                                             Authentication authentication) {

        var errors = errorService.checkErrors(bResult);
        if (errors != null){
            return ResponseEntity.badRequest().body(errors);
        }

        var result = calculatorService.currencyConvert(currencyGetDTO.getId(), currencyGetDTO.getAmount()
                ,authentication);

        return ResponseEntity.ok()
                .body(Map.of("result",result));
    }

    @PostMapping(value = "/ohms-law", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ohms_lawCalculate(@RequestBody @Valid OhmsLawGetDTO ohmsLawGetDTO,
                                               BindingResult bResult,
                                               Authentication authentication) {

        var errors = errorService.checkErrors(bResult);
        if (errors != null){
            return ResponseEntity.badRequest().body(errors);
        }

        if (Arrays.asList(ohmsLawGetDTO.getVoltage(), ohmsLawGetDTO.getCurrent(), ohmsLawGetDTO.getResistance())
                .stream()
                .filter(ob -> ob == null)
                .count() > 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Недопустимое кол-во значений (нужно 2)"));
        }

        var result = calculatorService.ohms_lawCalculate(ohmsLawGetDTO ,authentication);

        return ResponseEntity.ok()
                .body(Map.of("result",result));
    }

}
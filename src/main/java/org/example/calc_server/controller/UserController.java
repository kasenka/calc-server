package org.example.calc_server.controller;

import jakarta.validation.Valid;
import org.example.calc_server.dto.CurrencyGetDTO;
import org.example.calc_server.dto.OhmsLawGetDTO;

import org.example.calc_server.service.CalculatorService;
import org.example.calc_server.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class UserController {

    @Autowired
    private HistoryService historyService;

    @PostMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> userHistory(Authentication authentication) {

        var history = historyService.getUserHistory(authentication);

        return ResponseEntity.ok()
                .body(Map.of("history",history));
    }
}

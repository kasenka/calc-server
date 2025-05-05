package org.example.calc_server.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ErrorService {

    public Map<String, List<String>> checkErrors(BindingResult result) {
        if (result.hasErrors()){
            return Map.of("errors", result.getFieldErrors().stream()
                                            .map(error -> error.getDefaultMessage())
                                            .toList());
        }return null;
    }
}

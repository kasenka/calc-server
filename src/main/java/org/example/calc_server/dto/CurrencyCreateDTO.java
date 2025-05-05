package org.example.calc_server.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyCreateDTO {
    @NotBlank
    private String fromCurrency;

    @NotBlank
    private String toCurrency;

    @NotNull
    @Positive
    private Double rate;
}

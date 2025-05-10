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
    @NotBlank(message = "Значение не может быть пустым")
    private String fromCurrency;

    @NotBlank(message = "Значение не может быть пустым")
    private String toCurrency;

    @Positive(message = "Значение не может быть < 0")
    private Double rate;
}

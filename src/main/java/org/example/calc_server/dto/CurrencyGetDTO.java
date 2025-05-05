package org.example.calc_server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyGetDTO {

    @NotNull(message = "Выберите курс")
    private Long id;

    @Min(value = 0, message = "Минимальное значение - 0")
    private Double amount;
}

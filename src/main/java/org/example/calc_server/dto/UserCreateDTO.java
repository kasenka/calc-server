package org.example.calc_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    @NotBlank(message = "Юзернейм не может быть пустым")
    @Size(min = 3, max = 15, message = "Допустимая длина 3 - 15 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 3, message = "Допустимая длина не менее 3 символов")
    private String password;
}

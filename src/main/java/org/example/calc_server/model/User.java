package org.example.calc_server.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Юзернейм не может быть пустым")
    @Size(min = 3, max = 15, message = "Допустимая длина 3 - 15 символов")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 3, message = "Допустимая длина не менее 3 символов")
    private String encryptedPassword;

    @Column(nullable = false)
    private String role;

}

package org.example.calc_server.controller;

import jakarta.validation.Valid;
import org.example.calc_server.config.CustomUserDetailsService;
import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.dto.UserDTO;
import org.example.calc_server.jwt.JwtService;
import org.example.calc_server.mapper.UserMapper;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtService jwtService, AuthenticationManager authenticationManager,
                          UserMapper userMapper, CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody @Valid UserCreateDTO userCreateDTO,
                         BindingResult result){

        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existingValue, newValue) -> newValue
                    ));

            return ResponseEntity.badRequest().body(errors);
        }

        User user = userMapper.map(userCreateDTO);
        user.setEncryptedPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.map(user));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody @Valid UserCreateDTO userCreateDTO,
                                   BindingResult result){
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existingValue, newValue) -> newValue
                    ));

            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userCreateDTO.getUsername(),
                            userCreateDTO.getPassword()
                    )
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(userCreateDTO.getUsername());

            User user = (User) customUserDetailsService.loadUserByUsername(userCreateDTO.getUsername());

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "user", userMapper.map(user),
                            "jwt", jwt));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный логин или пароль"));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Пользователь не найден"));
        }
    }
}

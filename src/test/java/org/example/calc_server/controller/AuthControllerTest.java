package org.example.calc_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.calc_server.config.SecurityConfig;
import org.example.calc_server.dto.UserDTO;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.UserRepository;
import org.example.calc_server.dto.UserCreateDTO;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.suite.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Комплексные тесты для AuthController")
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UserCreateDTO userCreateDTO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Настраиваем MockMvc для работы с настоящим приложением
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        objectMapper = new ObjectMapper();

        userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername("testuser");
        userCreateDTO.setPassword("testpassword");

    }

    @AfterEach
    void clean(){
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Тесты регистрации")
    class RegistrationTests {

        @Test
        @DisplayName("Успешная регистрация")
        void register_success() throws Exception {

            MvcResult result = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.username").value(userCreateDTO.getUsername()),
                            jsonPath("$.role").value("USER")
                    )
                    .andDo(print())
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            UserDTO userDTO = objectMapper.readValue(responseContent, UserDTO.class);

            assertTrue(passwordEncoder.matches(userCreateDTO.getPassword(), userDTO.getEncryptedPassword()),
                    "Пароль должен быть корректно зашифрован");
        }

        @Test
        @DisplayName("Не уникальный юзернейм")
        void register_usernameTaken() throws Exception {
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Этот юзернейм уже занят"))
                    .andDo(print());
        }


        static Stream<Arguments> invalidUserProvider() {
            return Stream.of(
                    Arguments.of("", "validPassword1!", "Юзернейм не может быть пустым"),
                    Arguments.of("a", "validPassword1!", "Допустимая длина 3 - 15 символов"),
                    Arguments.of("a".repeat(51), "validPassword1!", "Допустимая длина 3 - 15 символов"),
                    Arguments.of("validUser", "", "Допустимая длина не менее 3 символов"),
                    Arguments.of("validUser", " ".repeat(10), "Пароль не может быть пустым"),
                    Arguments.of("validUser", "12", "Допустимая длина не менее 3 символов"),
                    Arguments.of("", "", "Юзернейм не может быть пустым")
            );
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("invalidUserProvider")
        @DisplayName("Невалидные данные")
        void register_validationError(String username, String password, String errorDescription) throws Exception {
            UserCreateDTO invalidDTO = new UserCreateDTO();
            invalidDTO.setUsername(username);
            invalidDTO.setPassword(password);

            String requestJson = objectMapper.writeValueAsString(invalidDTO);
            System.out.println(">>> JSON запроса: " + requestJson);

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
            .andExpectAll(
                    status().isBadRequest(),
                    jsonPath("$.errors").exists(),
                    jsonPath(("$.errors"), Matchers.hasItem(errorDescription)));
        }
    }

    @Nested
    @DisplayName("Тесты логина")
    public class LoginTests {

        @Test
        @DisplayName("Успешная авторизации")
        void login_success() throws Exception {
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
                    .andDo(print())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.user.username").value(userCreateDTO.getUsername()),
                            jsonPath("$.user.role").value("USER"),
                            jsonPath("$.jwt").exists(),
                            jsonPath("$.aut").exists()
                    ).andDo(print());
        }

        static Stream<Arguments> invalidUserLogin() {
            return Stream.of(
                    Arguments.of("wrongusername","testpassword","Неверный юзернейм "),
                    Arguments.of("testusername","wrongpassword","Неверный  пароль"),
                    Arguments.of("","","Неверный юзернейм и пароль"),
                    Arguments.of("   ","    ","Неверный юзернейм и пароль")
            );
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("invalidUserLogin")
        @DisplayName("Неверный юзернейм или пароль")
        void login_userNotFound(String username, String password, String error) throws Exception {

            UserCreateDTO invalidDTO = new UserCreateDTO();
            invalidDTO.setUsername(username);
            invalidDTO.setPassword(password);

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andDo(print())
                    .andExpectAll(
                            status().isUnauthorized(),
                            jsonPath("$.error").exists(),
                            jsonPath("$.error").value("Неверный юзернейм или пароль"));

        }

    }
}

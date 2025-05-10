package org.example.calc_server.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.example.calc_server.dto.CurrencyCreateDTO;
import org.example.calc_server.dto.CurrencyGetDTO;
import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.model.History;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.HistoryRepository;
import org.example.calc_server.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Комплексные тесты для AdminController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HistoryRepository historyRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setEncryptedPassword(passwordEncoder.encode("testpassword"));
        user.setRole("ADMIN");
        userRepository.save(user);

    }


    private HttpHeaders getHeader(String username, String password) throws Exception{
//      дто для логина
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername(username);
        userCreateDTO.setPassword(password);

//      запрос
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/login",
                userCreateDTO,
                String.class
        );

        JsonNode body = objectMapper.readTree(response.getBody());
        String jwt =  body.get("jwt").asText();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);

        return headers;

    }

    @AfterAll
    public void clear() {
        userRepository.deleteAll();
    }

    private void printResponse(ResponseEntity<String> response) {
        System.out.println(">>> RESPONSE STATUS:\n" + response.getStatusCode());
        System.out.println(">>> RESPONSE BODY:\n" + response.getBody());
    }

    @Test
    @DisplayName("Успешное сохранение нового курса")
    void createRate() throws Exception {
        CurrencyCreateDTO currencyCreateDTO = new CurrencyCreateDTO();
        currencyCreateDTO.setFromCurrency("USD");
        currencyCreateDTO.setToCurrency("EUR");
        currencyCreateDTO.setRate(0.8);

        HttpHeaders headers = getHeader("testuser", "testpassword");

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(currencyCreateDTO),
                headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/admin/createRate",
                request,
                String.class
        );

        JsonNode resultJson = objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(resultJson.has("currency"));

        printResponse(response);
    }


    static Stream<Arguments> invalidCurrency() {
        return Stream.of(
                Arguments.of("","EUR", 2, "Значение не может быть пустым"),
                Arguments.of("RUB","", 0.01, "Значение не может быть пустым"),
                Arguments.of("RUB","EUR", -1, "Значение не может быть < 0")
                );
    }


    @ParameterizedTest(name = "[{index}] {3}")
    @MethodSource("invalidCurrency")
    @DisplayName("Невалидные данные currencyCreateDTO")
    void currencyConvertError(String from, String to, double rate, String message) throws Exception {
        CurrencyCreateDTO currencyCreateDTO = new CurrencyCreateDTO();
        currencyCreateDTO.setFromCurrency(from);
        currencyCreateDTO.setToCurrency(to);
        currencyCreateDTO.setRate(rate);

        HttpHeaders headers = getHeader("testuser", "testpassword");

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(currencyCreateDTO),
                headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/admin/createRate",
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("errors"));
        assertTrue(resultJson.get("errors").toString().contains(message));

        printResponse(response);
    }

    @Test
    @DisplayName("Получение всей истории")
    void users() throws Exception{
        faker = new Faker();

        for (int i = 0; i < 5; i++) {
            History history = new History();
            history.setUsername(faker.name().firstName());
            history.setAction("From -> To Currency");
            history.setValues(faker.currency() + "->" + faker.currency()
                    + "; amount = " + faker.number().numberBetween(1, 100));
            history.setResult(String.valueOf(faker.number()));
            historyRepository.save(history);
        }


        HttpHeaders headers = getHeader("testuser", "testpassword");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/calculator/admin/usersHistory",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("allHistory"));

        printResponse(response);
    }


    @Test
    @DisplayName("Успешное удаление юзера")
    void deleteUser() throws Exception {
        HttpHeaders headers = getHeader("testuser", "testpassword");

        User user = new User();
        user.setUsername("user");
        user.setEncryptedPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        userRepository.save(user);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/calculator/admin/" + user.getId() + "/delete",
                HttpMethod.DELETE,
                request,
                String.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        printResponse(response);
    }
}

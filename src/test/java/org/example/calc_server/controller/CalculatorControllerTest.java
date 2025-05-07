package org.example.calc_server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.example.calc_server.dto.CurrencyGetDTO;
import org.example.calc_server.dto.OhmsLawGetDTO;
import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.CurrencyRateRepository;
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

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Комплексные тесты для CalculatorController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CalculatorControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static Faker faker;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

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
        user.setRole("USER");
        userRepository.save(user);


        faker = new Faker(new Locale("ru"));

        for (int i = 0; i < 5; i++) {
            CurrencyRate currencyRate = new CurrencyRate();
            currencyRate.setRate(faker.number().randomDouble(3, 0, 100)); // Случайный курс от 0 до 100 с 3 знаками
            currencyRate.setFromCurrency(faker.currency().code());
            currencyRate.setToCurrency(faker.currency().code());

            currencyRateRepository.save(currencyRate);
        }
    }

    @AfterAll
    public void clear() {
        currencyRateRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    public void cleanUp() {
        historyRepository.deleteAll();
    }

    private void printResponse(ResponseEntity<String> response) {
        System.out.println(">>> RESPONSE STATUS:\n" + response.getStatusCode());
        System.out.println(">>> RESPONSE BODY:\n" + response.getBody());
    }


    private HttpHeaders loginAndGetHeaders() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername("testuser");
        userCreateDTO.setPassword("testpassword");


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


    @Test
    @DisplayName("Получение курсов")
    void showCalculator() throws Exception {

        HttpHeaders headers = loginAndGetHeaders();

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/calculator",
                HttpMethod.GET,
                request,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        printResponse(response);
    }


    @Test
    @DisplayName("Успешнный расчет currency")
    void currencyConvert() throws Exception {
        // Готовим DTO
        CurrencyGetDTO currencyGetDTO = new CurrencyGetDTO();
        currencyGetDTO.setId(currencyRateRepository.findAny().get().getId());
        currencyGetDTO.setAmount(10d);

        HttpHeaders headers = loginAndGetHeaders();

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(currencyGetDTO),
                headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/currency",
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("result"));
        assertTrue(historyRepository.findAllByUsername("testuser").size() == 1);

        System.out.println(">>>>>>> HISTORY BY " +
                historyRepository.findAllByUsername("testuser").stream().findFirst().get().toString());

        printResponse(response);
    }


    static Stream<Arguments> invalidAmount() {
        return Stream.of(
                Arguments.of(-31, "Минимальное значение - 0"),
                Arguments.of(-1, "Минимальное значение - 0")
        );
    }


    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("invalidAmount")
    @DisplayName("Отрицательное значение amount")
    void currencyConvertError( double amount, String message) throws Exception {
        CurrencyGetDTO currencyGetDTO = new CurrencyGetDTO();
        currencyGetDTO.setId(currencyRateRepository.findAny().get().getId());
        currencyGetDTO.setAmount(amount);

        HttpHeaders headers = loginAndGetHeaders();

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(currencyGetDTO),
                headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/currency",
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("errors"));
        assertTrue(resultJson.get("errors").toString().contains(message));

        printResponse(response);
    }

    static Stream<Arguments> validOmhsGet() {
        return Stream.of(
                Arguments.of(
                        null,
                        faker.number().randomDouble(3, 0, 100),
                        faker.number().randomDouble(3, 0, 100),
                        "current"),
                Arguments.of(
                        faker.number().randomDouble(3, 0, 100),
                        null,
                        faker.number().randomDouble(3, 0, 100),
                        "resistance"),
                Arguments.of(
                        faker.number().randomDouble(3, 0, 100),
                        faker.number().randomDouble(3, 0, 100),
                        null,
                        "voltage")
        );
    }
    @ParameterizedTest(name = "[{index}] {3}")
    @MethodSource("validOmhsGet")
    @DisplayName("Успешнный расчет Omhs")
    void ohms_lawCalculate(Double current, Double resistance, Double voltage) throws Exception {
        OhmsLawGetDTO ohmsLawGetDTO = new OhmsLawGetDTO();
        ohmsLawGetDTO.setCurrent(current);
        ohmsLawGetDTO.setResistance(resistance);
        ohmsLawGetDTO.setVoltage(voltage);

        HttpHeaders headers = loginAndGetHeaders();

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(ohmsLawGetDTO),
                headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/ohms-law",
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("result"));
        assertTrue(historyRepository.findAllByUsername("testuser").size() == 1);

        System.out.println(">>>>>>> HISTORY BY " +
                historyRepository.findAllByUsername("testuser").stream().findFirst().get().toString());

        printResponse(response);
    }


    static Stream<Arguments> invalidOmhsGet() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        faker.number().randomDouble(3, 0, 100),
                        "2 null"),
                Arguments.of(
                        null,
                        null,
                        null,
                        "3 null"),
                Arguments.of(
                        faker.number().randomDouble(3, 0, 100),
                        faker.number().randomDouble(3, 0, 100),
                        faker.number().randomDouble(3, 0, 100),
                        "0 null")
        );
    }
    @ParameterizedTest(name = "[{index}] {3}")
    @MethodSource("invalidOmhsGet")
    @DisplayName("Невалидное кол-во значений Omhs")
    void ohms_lawCalculateError(Double current, Double resistance, Double voltage) throws Exception {
        OhmsLawGetDTO ohmsLawGetDTO = new OhmsLawGetDTO();
        ohmsLawGetDTO.setCurrent(current);
        ohmsLawGetDTO.setResistance(resistance);
        ohmsLawGetDTO.setVoltage(voltage);

        HttpHeaders headers = loginAndGetHeaders();

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(ohmsLawGetDTO),
                headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/calculator/ohms-law",
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        JsonNode resultJson = objectMapper.readTree(response.getBody());
        assertTrue(resultJson.has("error"));
        assertEquals(resultJson.get("error").asText(), "Недопустимое кол-во значений (нужно 2)");

        printResponse(response);
    }
}

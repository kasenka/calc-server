package org.example.calc_server.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.model.History;
import org.example.calc_server.model.User;
import org.example.calc_server.repository.CurrencyRateRepository;
import org.example.calc_server.repository.HistoryRepository;
import org.example.calc_server.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Комплексные тесты для UserController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

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
        faker = new Faker();

        for (int i = 0; i < 5; i++) {
            History history = new History();
            history.setUsername("testuser");
            history.setAction("From -> To Currency");
            history.setValues(faker.currency() + "->" + faker.currency()
                    + "; amount = " + faker.number().numberBetween(1, 100));
            history.setResult("100");
            historyRepository.save(history);
        }
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
        historyRepository.deleteAll();
    }


    @Test
    @DisplayName("Получение истории юзера")
    void userHistory() throws Exception{
        User user = new User();
        user.setUsername("testuser");
        user.setEncryptedPassword(passwordEncoder.encode("testpassword"));
        user.setRole("USER");
        userRepository.save(user);


        HttpHeaders headers = getHeader("testuser", "testpassword");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/calculator/user/history",
                HttpMethod.GET,
                request,
                String.class);

        JsonNode resultJson = objectMapper.readTree(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(resultJson.has("history"));
        assertTrue(resultJson.get("history").size() == 5);

        System.out.println(">>>>>>> HISTORY BY testuser");
        for (History history: historyRepository.findAllByUsername("testuser")){
            System.out.println(history.toString());
        }

        System.out.println(">>> RESPONSE STATUS:\n" + response.getStatusCode());
        System.out.println(">>> RESPONSE BODY:\n" + response.getBody());
    }


    @Test
    @DisplayName("Роль ADMIN - ошибка доступа")
    void userHistoryError() throws Exception{
        User user = new User();
        user.setUsername("testadmin");
        user.setEncryptedPassword(passwordEncoder.encode("testadminpassword"));
        user.setRole("ADMIN");
        userRepository.save(user);


        HttpHeaders headers = getHeader("testadmin", "testadminpassword");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/calculator/user/history",
                HttpMethod.GET,
                request,
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        System.out.println(">>> RESPONSE STATUS:\n" + response.getStatusCode());
        System.out.println(">>> RESPONSE BODY:\n" + response.getBody());
    }
}

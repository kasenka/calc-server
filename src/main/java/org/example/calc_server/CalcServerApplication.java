package org.example.calc_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CalcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalcServerApplication.class, args);
    }

}

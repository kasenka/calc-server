package org.example.calc_server.repository;

import org.example.calc_server.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static javax.swing.text.html.HTML.Tag.SELECT;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    @Query(value = "SELECT * FROM currency_rate LIMIT 1", nativeQuery = true)
    Optional<CurrencyRate> findAny();
}

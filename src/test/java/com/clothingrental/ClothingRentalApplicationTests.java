package com.clothingrental;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testes de fundação: garantem que a aplicação sobe e consegue falar com o banco.
 *
 * <p>Requer o PostgreSQL em execução ({@code docker compose up -d}) e o arquivo {@code .env}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClothingRentalApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        // Se o contexto do Spring não carregar, este teste falha antes de chegar aqui.
    }

    @Test
    void databaseConnectionIsValid() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
    }
}

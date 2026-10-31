package com.clothingrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação.
 *
 * <p>Por estar no pacote raiz {@code com.clothingrental}, a anotação
 * {@link SpringBootApplication} encontra automaticamente os componentes de todos
 * os módulos (auth, catalog, customer, rentalrequest, reservation e shared).
 */
@SpringBootApplication
public class ClothingRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClothingRentalApplication.class, args);
    }
}

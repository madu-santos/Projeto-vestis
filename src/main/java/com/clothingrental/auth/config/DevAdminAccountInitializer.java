package com.clothingrental.auth.config;

import com.clothingrental.auth.service.StaffAccountService;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Inicializador de desenvolvimento: prepara o primeiro estabelecimento (tenant)
 * e o primeiro administrador da loja no perfil 'dev'.
 *
 * <p>Executa de forma idempotente:
 * - Se já existirem administradores, não faz nada.
 * - Se a loja já existir, apenas reutiliza-a.
 * - Se o banco estiver vazio, cria a primeira loja e o primeiro administrador.
 */
@Component
@Profile("dev")
public class DevAdminAccountInitializer implements ApplicationRunner {

    static final String STORE_NAME_VARIABLE = "STORE_NAME";
    static final String STORE_SLUG_VARIABLE = "STORE_SLUG";
    static final String ADMIN_USERNAME_VARIABLE = "ADMIN_USERNAME";
    static final String ADMIN_PASSWORD_VARIABLE = "ADMIN_PASSWORD";

    private static final Logger log = LoggerFactory.getLogger(DevAdminAccountInitializer.class);

    private final StoreService storeService;
    private final StaffAccountService staffAccountService;
    private final Environment environment;

    public DevAdminAccountInitializer(StoreService storeService,
                                      StaffAccountService staffAccountService,
                                      Environment environment) {
        this.storeService = storeService;
        this.staffAccountService = staffAccountService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (staffAccountService.hasAnyAccount()) {
            log.info("Contas administrativas já existentes: inicializador concluído.");
            return;
        }

        String storeSlug = environment.getProperty(STORE_SLUG_VARIABLE, "").trim();
        String storeName = environment.getProperty(STORE_NAME_VARIABLE, "").trim();
        String username = environment.getProperty(ADMIN_USERNAME_VARIABLE, "").trim();
        String password = environment.getProperty(ADMIN_PASSWORD_VARIABLE, "");

        if (storeName.isBlank() || storeSlug.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Nenhum estabelecimento ou conta administrativa encontrada. Defina "
                            + STORE_NAME_VARIABLE + ", " + STORE_SLUG_VARIABLE + ", "
                            + ADMIN_USERNAME_VARIABLE + " e " + ADMIN_PASSWORD_VARIABLE
                            + " (senha com no mínimo " + StaffAccountService.PASSWORD_MIN_LENGTH
                            + " caracteres) no arquivo .env e reinicie a aplicação.");
        }

        Store store = storeService.findBySlug(storeSlug)
                .or(() -> storeService.findBySlug("loja-padrao-migrada"))
                .orElseGet(() -> storeService.createStore(storeName, storeSlug));

        try {
            staffAccountService.createAccount(username, password, store);
            log.info("Primeiro estabelecimento '{}' e conta administrativa '{}' criados com sucesso.", store.getSlug(), username);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Configuração inválida para o primeiro administrador: " + e.getMessage(), e);
        }
    }
}

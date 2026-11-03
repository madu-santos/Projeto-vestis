package com.clothingrental.auth.config;

import com.clothingrental.auth.service.StaffAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Cria a primeira conta administrativa no perfil de desenvolvimento.
 *
 * <p>Só age quando ainda não existe nenhuma conta. Os dados vêm das variáveis
 * ADMIN_USERNAME e ADMIN_PASSWORD (por exemplo, definidas no arquivo .env).
 * Se elas faltarem ou forem inválidas, a aplicação não sobe e informa o que configurar.
 */
@Component
@Profile("dev")
public class DevAdminAccountInitializer implements ApplicationRunner {

    static final String USERNAME_VARIABLE = "ADMIN_USERNAME";
    static final String PASSWORD_VARIABLE = "ADMIN_PASSWORD";

    private static final Logger log = LoggerFactory.getLogger(DevAdminAccountInitializer.class);

    private final StaffAccountService staffAccountService;
    private final Environment environment;

    public DevAdminAccountInitializer(StaffAccountService staffAccountService, Environment environment) {
        this.staffAccountService = staffAccountService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (staffAccountService.hasAnyAccount()) {
            log.info("Conta administrativa já existente: nenhuma conta será criada ou alterada.");
            return;
        }

        // A senha é lida apenas aqui e não fica guardada em atributos da classe.
        String username = environment.getProperty(USERNAME_VARIABLE, "");
        String password = environment.getProperty(PASSWORD_VARIABLE, "");

        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Nenhuma conta administrativa encontrada. Defina " + USERNAME_VARIABLE + " e "
                    + PASSWORD_VARIABLE + " (senha com no mínimo " + StaffAccountService.PASSWORD_MIN_LENGTH
                    + " caracteres) no arquivo .env e reinicie a aplicação.");
        }

        try {
            staffAccountService.createAccount(username, password);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Configuração inválida do primeiro administrador: " + e.getMessage(), e);
        }
        log.info("Primeira conta administrativa criada para o usuário '{}'.", username.trim());
    }
}

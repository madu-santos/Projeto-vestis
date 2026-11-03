package com.clothingrental.auth.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.service.StaffAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class DevAdminAccountInitializerTest {

    private static final String PASSWORD = "initializer-test-password";

    @Mock
    private StaffAccountService staffAccountService;

    private final MockEnvironment environment = new MockEnvironment();

    @Test
    void doesNotCreateOrChangeAnythingWhenAnAccountAlreadyExists() {
        when(staffAccountService.hasAnyAccount()).thenReturn(true);
        environment.setProperty("ADMIN_USERNAME", "admin");
        environment.setProperty("ADMIN_PASSWORD", PASSWORD);

        runInitializer();

        verify(staffAccountService, never()).createAccount(any(), any());
    }

    @Test
    void createsFirstAccountFromEnvironmentVariables() {
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        environment.setProperty("ADMIN_USERNAME", "admin");
        environment.setProperty("ADMIN_PASSWORD", PASSWORD);

        runInitializer();

        verify(staffAccountService).createAccount("admin", PASSWORD);
    }

    @Test
    void failsWithClearMessageWhenVariablesAreMissing() {
        when(staffAccountService.hasAnyAccount()).thenReturn(false);

        assertThatThrownBy(this::runInitializer)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_USERNAME")
                .hasMessageContaining("ADMIN_PASSWORD");
        verify(staffAccountService, never()).createAccount(any(), any());
    }

    @Test
    void invalidConfigurationFailsWithoutRevealingThePassword() {
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        when(staffAccountService.createAccount("admin", PASSWORD))
                .thenThrow(new IllegalArgumentException("A senha deve ter no mínimo 12 caracteres."));
        environment.setProperty("ADMIN_USERNAME", "admin");
        environment.setProperty("ADMIN_PASSWORD", PASSWORD);

        assertThatThrownBy(this::runInitializer)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("primeiro administrador")
                .hasMessageNotContaining(PASSWORD);
    }

    private void runInitializer() {
        new DevAdminAccountInitializer(staffAccountService, environment).run(new DefaultApplicationArguments());
    }
}

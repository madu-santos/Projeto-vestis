package com.clothingrental.auth.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.service.StaffAccountService;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.service.StoreService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class DevAdminAccountInitializerTest {

    private static final String STORE_NAME = "Loja Demo";
    private static final String STORE_SLUG = "loja-demo";
    private static final String ADMIN_USER = "admin";
    private static final String PASSWORD = "initializer-test-password";

    @Mock
    private StoreService storeService;

    @Mock
    private StaffAccountService staffAccountService;

    private final MockEnvironment environment = new MockEnvironment();

    @Test
    void doesNotCreateOrChangeAnythingWhenAnAccountAlreadyExists() {
        when(staffAccountService.hasAnyAccount()).thenReturn(true);
        environment.setProperty("STORE_NAME", STORE_NAME);
        environment.setProperty("STORE_SLUG", STORE_SLUG);
        environment.setProperty("ADMIN_USERNAME", ADMIN_USER);
        environment.setProperty("ADMIN_PASSWORD", PASSWORD);

        runInitializer();

        verify(storeService, never()).createStore(any(), any());
        verify(staffAccountService, never()).createAccount(any(), any(), any());
    }

    @Test
    void createsStoreAndFirstAccountWhenNoneExist() {
        Store createdStore = new Store(STORE_NAME, STORE_SLUG);
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        when(storeService.findBySlug(STORE_SLUG)).thenReturn(Optional.empty());
        when(storeService.createStore(STORE_NAME, STORE_SLUG)).thenReturn(createdStore);

        setAllEnvironmentProperties();

        runInitializer();

        verify(storeService).createStore(STORE_NAME, STORE_SLUG);
        verify(staffAccountService).createAccount(ADMIN_USER, PASSWORD, createdStore);
    }

    @Test
    void reusesExistingStoreWhenStoreAlreadyExists() {
        Store existingStore = new Store(STORE_NAME, STORE_SLUG);
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        when(storeService.findBySlug(STORE_SLUG)).thenReturn(Optional.of(existingStore));

        setAllEnvironmentProperties();

        runInitializer();

        verify(storeService, never()).createStore(any(), any());
        verify(staffAccountService).createAccount(ADMIN_USER, PASSWORD, existingStore);
    }

    @Test
    void reusesMigratedStoreWhenStoreSlugNotFoundButMigratedStoreExists() {
        Store migratedStore = new Store("Loja Padrao Migrada", "loja-padrao-migrada");
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        when(storeService.findBySlug(STORE_SLUG)).thenReturn(Optional.empty());
        when(storeService.findBySlug("loja-padrao-migrada")).thenReturn(Optional.of(migratedStore));

        setAllEnvironmentProperties();

        runInitializer();

        verify(storeService, never()).createStore(any(), any());
        verify(staffAccountService).createAccount(ADMIN_USER, PASSWORD, migratedStore);
    }

    @Test
    void failsWithClearMessageWhenVariablesAreMissing() {
        when(staffAccountService.hasAnyAccount()).thenReturn(false);

        assertThatThrownBy(this::runInitializer)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STORE_NAME")
                .hasMessageContaining("STORE_SLUG")
                .hasMessageContaining("ADMIN_USERNAME")
                .hasMessageContaining("ADMIN_PASSWORD");

        verify(storeService, never()).createStore(any(), any());
        verify(staffAccountService, never()).createAccount(any(), any(), any());
    }

    @Test
    void invalidConfigurationFailsWithoutRevealingThePassword() {
        Store store = new Store(STORE_NAME, STORE_SLUG);
        when(staffAccountService.hasAnyAccount()).thenReturn(false);
        when(storeService.findBySlug(STORE_SLUG)).thenReturn(Optional.of(store));
        when(staffAccountService.createAccount(ADMIN_USER, PASSWORD, store))
                .thenThrow(new IllegalArgumentException("A senha deve ter no mínimo 12 caracteres."));

        setAllEnvironmentProperties();

        assertThatThrownBy(this::runInitializer)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("primeiro administrador")
                .hasMessageNotContaining(PASSWORD);
    }

    private void setAllEnvironmentProperties() {
        environment.setProperty("STORE_NAME", STORE_NAME);
        environment.setProperty("STORE_SLUG", STORE_SLUG);
        environment.setProperty("ADMIN_USERNAME", ADMIN_USER);
        environment.setProperty("ADMIN_PASSWORD", PASSWORD);
    }

    private void runInitializer() {
        new DevAdminAccountInitializer(storeService, staffAccountService, environment)
                .run(new DefaultApplicationArguments());
    }
}

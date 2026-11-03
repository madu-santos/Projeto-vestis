package com.clothingrental.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.exception.NoCurrentStoreContextException;
import com.clothingrental.tenant.exception.StoreDisabledException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentStoreServiceTest {

    private CurrentStoreService currentStoreService;

    @BeforeEach
    void setUp() {
        currentStoreService = new CurrentStoreService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsStoreIdWhenStaffAccountPrincipalIsAuthenticated() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(
                10L,
                "maria.silva",
                "encoded-hash",
                5L,
                true
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Long storeId = currentStoreService.getCurrentStoreId();

        assertThat(storeId).isEqualTo(5L);
    }

    @Test
    void throwsExceptionWhenAuthenticationIsNull() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(null);
        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> currentStoreService.getCurrentStoreId())
                .isInstanceOf(NoCurrentStoreContextException.class)
                .hasMessageContaining("autenticação prévia");
    }

    @Test
    void throwsExceptionWhenPrincipalIsNotStaffAccountPrincipal() {
        Authentication auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> currentStoreService.getCurrentStoreId())
                .isInstanceOf(NoCurrentStoreContextException.class)
                .hasMessageContaining("não é uma conta administrativa");
    }

    @Test
    void throwsExceptionWhenPrincipalHasNullStoreId() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(
                10L,
                "admin.sem.loja",
                "encoded-hash",
                null,
                true
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> currentStoreService.getCurrentStoreId())
                .isInstanceOf(NoCurrentStoreContextException.class)
                .hasMessageContaining("não possui uma loja associada");
    }

    @Test
    void throwsExceptionWhenStoreIsDisabled() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(
                10L,
                "maria.silva",
                "encoded-hash",
                5L,
                true,
                false // storeEnabled = false
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> currentStoreService.getCurrentStoreId())
                .isInstanceOf(StoreDisabledException.class)
                .hasMessageContaining("desativada");
    }
}

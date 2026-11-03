package com.clothingrental.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.repository.StoreRepository;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class AdminStoreActiveAuthorizationManagerTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private RequestAuthorizationContext context;

    @InjectMocks
    private AdminStoreActiveAuthorizationManager authorizationManager;

    @Test
    void allowsAccessWhenAuthenticatedWithAdminRoleAndStoreIsActiveInDatabase() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(1L, "admin", "hash", 10L, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(storeRepository.existsByIdAndEnabledTrue(10L)).thenReturn(true);

        AuthorizationDecision decision = authorizationManager.authorize(() -> auth, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void deniesAccessWhenStoreIsDisabledInDatabase() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(1L, "admin", "hash", 10L, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(storeRepository.existsByIdAndEnabledTrue(10L)).thenReturn(false);

        AuthorizationDecision decision = authorizationManager.authorize(() -> auth, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void deniesAccessWhenUnauthenticated() {
        AuthorizationDecision decision = authorizationManager.authorize(() -> null, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        verifyNoInteractions(storeRepository);
    }

    @Test
    void deniesAccessWhenUserLacksAdminRole() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(
                1L, "user", "hash", 10L, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        AuthorizationDecision decision = authorizationManager.authorize(() -> auth, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        verifyNoInteractions(storeRepository);
    }

    @Test
    void deniesAccessWhenPrincipalIsNotStaffAccountPrincipal() {
        Authentication auth = new AnonymousAuthenticationToken(
                "key", "anon", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        AuthorizationDecision decision = authorizationManager.authorize(() -> auth, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        verifyNoInteractions(storeRepository);
    }

    @Test
    void deniesAccessWhenPrincipalStoreIdIsNull() {
        StaffAccountPrincipal principal = new StaffAccountPrincipal(1L, "admin", "hash", null, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        AuthorizationDecision decision = authorizationManager.authorize(() -> auth, context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        verifyNoInteractions(storeRepository);
    }
}

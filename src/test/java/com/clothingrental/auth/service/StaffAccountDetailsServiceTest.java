package com.clothingrental.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.auth.repository.StaffAccountRepository;
import com.clothingrental.tenant.model.Store;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StaffAccountDetailsServiceTest {

    @Mock
    private StaffAccountRepository staffAccountRepository;

    @InjectMocks
    private StaffAccountDetailsService staffAccountDetailsService;

    private Store sampleStore;

    @BeforeEach
    void setUp() {
        sampleStore = new Store("Loja Aurora", "aurora");
        ReflectionTestUtils.setField(sampleStore, "id", 1L);
    }

    @Test
    void loadsActiveAccountWithAdminAuthorityAndStoreContext() {
        StaffAccount account = new StaffAccount("admin", "stored-hash", sampleStore);
        ReflectionTestUtils.setField(account, "id", 42L);
        when(staffAccountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        UserDetails details = staffAccountDetailsService.loadUserByUsername("admin");

        assertThat(details).isInstanceOf(StaffAccountPrincipal.class);
        StaffAccountPrincipal principal = (StaffAccountPrincipal) details;

        assertThat(principal.getId()).isEqualTo(42L);
        assertThat(principal.getUsername()).isEqualTo("admin");
        assertThat(principal.getPassword()).isEqualTo("stored-hash");
        assertThat(principal.getStoreId()).isEqualTo(1L);
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadsAccountCaseInsensitivelyAndTrimmed() {
        StaffAccount account = new StaffAccount("admin", "stored-hash", sampleStore);
        when(staffAccountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        UserDetails details = staffAccountDetailsService.loadUserByUsername("  ADMIN  ");

        assertThat(details.getUsername()).isEqualTo("admin");
    }

    @Test
    void disabledAccountIsLoadedAsDisabled() {
        StaffAccount account = new StaffAccount("former", "stored-hash", sampleStore);
        account.disable();
        when(staffAccountRepository.findByUsername("former")).thenReturn(Optional.of(account));

        assertThat(staffAccountDetailsService.loadUserByUsername("former").isEnabled()).isFalse();
    }

    @Test
    void unknownUsernameThrowsUsernameNotFoundException() {
        when(staffAccountRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffAccountDetailsService.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void accountWithDisabledStoreIsLoadedAsDisabledAndLocked() {
        sampleStore.disable();
        StaffAccount account = new StaffAccount("active.staff", "stored-hash", sampleStore);
        when(staffAccountRepository.findByUsername("active.staff")).thenReturn(Optional.of(account));

        UserDetails details = staffAccountDetailsService.loadUserByUsername("active.staff");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(((StaffAccountPrincipal) details).isStoreEnabled()).isFalse();
    }

    @Test
    void reactivatingStoreRestoresStaffAccessWithoutAlteringIndividualAccounts() {
        sampleStore.disable();
        StaffAccount activeStaff = new StaffAccount("active.staff", "stored-hash", sampleStore);
        StaffAccount disabledStaff = new StaffAccount("disabled.staff", "stored-hash", sampleStore);
        disabledStaff.disable();

        when(staffAccountRepository.findByUsername("active.staff")).thenReturn(Optional.of(activeStaff));
        when(staffAccountRepository.findByUsername("disabled.staff")).thenReturn(Optional.of(disabledStaff));

        // Com a loja desativada, nenhum dos dois consegue autenticar
        assertThat(staffAccountDetailsService.loadUserByUsername("active.staff").isEnabled()).isFalse();
        assertThat(staffAccountDetailsService.loadUserByUsername("disabled.staff").isEnabled()).isFalse();

        // Reativando a loja: a conta do funcionário que estava ativa volta a ter acesso,
        // mas o funcionário individualmente desativado permanece desativado
        sampleStore.enable();

        assertThat(staffAccountDetailsService.loadUserByUsername("active.staff").isEnabled()).isTrue();
        assertThat(staffAccountDetailsService.loadUserByUsername("disabled.staff").isEnabled()).isFalse();
        assertThat(disabledStaff.isEnabled()).isFalse();
    }
}

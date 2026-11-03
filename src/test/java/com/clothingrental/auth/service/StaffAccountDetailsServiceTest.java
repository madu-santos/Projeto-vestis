package com.clothingrental.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class StaffAccountDetailsServiceTest {

    @Mock
    private StaffAccountRepository staffAccountRepository;

    @InjectMocks
    private StaffAccountDetailsService staffAccountDetailsService;

    @Test
    void loadsActiveAccountWithAdminAuthority() {
        StaffAccount account = new StaffAccount("admin", "stored-hash");
        when(staffAccountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        UserDetails details = staffAccountDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("stored-hash");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadsAccountCaseInsensitivelyAndTrimmed() {
        StaffAccount account = new StaffAccount("admin", "stored-hash");
        when(staffAccountRepository.findByUsername("admin")).thenReturn(Optional.of(account));

        UserDetails details = staffAccountDetailsService.loadUserByUsername("  ADMIN  ");

        assertThat(details.getUsername()).isEqualTo("admin");
    }

    @Test
    void disabledAccountIsLoadedAsDisabled() {
        StaffAccount account = new StaffAccount("former", "stored-hash");
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
}

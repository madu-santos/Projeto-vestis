package com.clothingrental.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Testes unitários: o repositório e o encoder são simulados com Mockito, sem banco de dados. */
@ExtendWith(MockitoExtension.class)
class StaffAccountServiceTest {

    private static final String VALID_PASSWORD = "unit-test-password";

    @Mock
    private StaffAccountRepository staffAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StaffAccountService staffAccountService;

    @Test
    void createAccountStoresOnlyTheEncodedPassword() {
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encoded-hash");
        when(staffAccountRepository.save(any(StaffAccount.class))).thenAnswer(call -> call.getArgument(0));

        StaffAccount account = staffAccountService.createAccount("  admin  ", VALID_PASSWORD);

        assertThat(account.getUsername()).isEqualTo("admin");
        assertThat(account.getPasswordHash()).isEqualTo("encoded-hash");
        assertThat(account.isEnabled()).isTrue();
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void createAccountNormalizesUsernameToLowerCase() {
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encoded-hash");
        when(staffAccountRepository.save(any(StaffAccount.class))).thenAnswer(call -> call.getArgument(0));

        StaffAccount account = staffAccountService.createAccount("  AdMiN.UsEr  ", VALID_PASSWORD);

        assertThat(account.getUsername()).isEqualTo("admin.user");
    }

    @Test
    void rejectsPasswordShorterThanMinimumWithoutRevealingIt() {
        String shortPassword = "short-pass1";

        assertThatThrownBy(() -> staffAccountService.createAccount("admin", shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(StaffAccountService.PASSWORD_MIN_LENGTH))
                .hasMessageNotContaining(shortPassword);
        verifyNoInteractions(staffAccountRepository, passwordEncoder);
    }

    @Test
    void rejectsPasswordLongerThanBcryptLimit() {
        String longPassword = "a".repeat(StaffAccountService.PASSWORD_MAX_BYTES + 1);

        assertThatThrownBy(() -> staffAccountService.createAccount("admin", longPassword))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(staffAccountRepository, passwordEncoder);
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> staffAccountService.createAccount("   ", VALID_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(staffAccountRepository, passwordEncoder);
    }

    @Test
    void rejectsUsernameLongerThanMaximum() {
        String longUsername = "u".repeat(StaffAccount.USERNAME_MAX_LENGTH + 1);

        assertThatThrownBy(() -> staffAccountService.createAccount(longUsername, VALID_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(staffAccountRepository, passwordEncoder);
    }

    @Test
    void hasAnyAccountReflectsRepositoryCount() {
        when(staffAccountRepository.count()).thenReturn(0L, 1L);

        assertThat(staffAccountService.hasAnyAccount()).isFalse();
        assertThat(staffAccountService.hasAnyAccount()).isTrue();
    }
}

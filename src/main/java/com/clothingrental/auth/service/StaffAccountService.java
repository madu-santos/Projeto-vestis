package com.clothingrental.auth.service;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import com.clothingrental.tenant.model.Store;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operações sobre contas administrativas vinculadas a um estabelecimento (tenant).
 *
 * <p>Concentra as regras de criação de conta (validação, unicidade global do username,
 * vinculação à loja e codificação da senha), para que nenhuma outra classe precise
 * lidar com a senha em texto puro.
 */
@Service
public class StaffAccountService {

    public static final int PASSWORD_MIN_LENGTH = 12;

    /** O BCrypt considera no máximo 72 bytes da senha; acima disso o Spring Security rejeita. */
    static final int PASSWORD_MAX_BYTES = 72;

    private final StaffAccountRepository staffAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountService(StaffAccountRepository staffAccountRepository, PasswordEncoder passwordEncoder) {
        this.staffAccountRepository = staffAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public boolean hasAnyAccount() {
        return staffAccountRepository.count() > 0;
    }

    @Transactional(readOnly = true)
    public Optional<StaffAccount> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return staffAccountRepository.findByUsername(username.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Cria uma conta ativa vinculada a uma loja, armazenando apenas o hash BCrypt da senha.
     *
     * @throws IllegalArgumentException se o usuário, a senha ou a loja não atenderem às regras
     */
    @Transactional
    public StaffAccount createAccount(String username, String rawPassword, Store store) {
        if (store == null) {
            throw new IllegalArgumentException("A conta administrativa deve pertencer obrigatoriamente a um estabelecimento.");
        }
        String normalizedUsername = validateUsername(username);
        validatePassword(rawPassword);

        if (staffAccountRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("O nome de usuário '" + normalizedUsername + "' já está em uso na plataforma.");
        }

        StaffAccount account = new StaffAccount(normalizedUsername, passwordEncoder.encode(rawPassword), store);
        return staffAccountRepository.save(account);
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("O nome de usuário é obrigatório.");
        }
        String trimmed = username.trim().toLowerCase(Locale.ROOT);
        if (trimmed.length() > StaffAccount.USERNAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O nome de usuário deve ter no máximo " + StaffAccount.USERNAME_MAX_LENGTH + " caracteres.");
        }
        return trimmed;
    }

    // As mensagens nunca incluem a senha recebida.
    private void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo " + PASSWORD_MIN_LENGTH + " caracteres.");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES) {
            throw new IllegalArgumentException(
                    "A senha deve ter no máximo " + PASSWORD_MAX_BYTES + " bytes.");
        }
    }
}

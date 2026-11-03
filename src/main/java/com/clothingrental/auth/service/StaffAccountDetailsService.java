package com.clothingrental.auth.service;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ponte entre as contas persistidas e o Spring Security.
 *
 * <p>O Spring Security chama este serviço durante o login para carregar o usuário;
 * a comparação da senha digitada com o hash é feita pelo próprio framework,
 * usando o {@code PasswordEncoder} configurado.
 */
@Service
public class StaffAccountDetailsService implements UserDetailsService {

    static final String ADMIN_ROLE = "ADMIN";

    private final StaffAccountRepository staffAccountRepository;

    public StaffAccountDetailsService(StaffAccountRepository staffAccountRepository) {
        this.staffAccountRepository = staffAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return staffAccountRepository.findByUsername(normalizedUsername)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Conta administrativa não encontrada."));
    }

    private UserDetails toUserDetails(StaffAccount account) {
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .disabled(!account.isEnabled())
                // roles("ADMIN") gera a autoridade "ROLE_ADMIN".
                .roles(ADMIN_ROLE)
                .build();
    }
}

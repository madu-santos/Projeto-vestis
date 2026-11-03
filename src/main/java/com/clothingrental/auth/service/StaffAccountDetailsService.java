package com.clothingrental.auth.service;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.auth.repository.StaffAccountRepository;
import java.util.Locale;
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
        Long storeId = account.getStore() != null ? account.getStore().getId() : null;
        boolean storeEnabled = account.getStore() != null && account.getStore().isEnabled();
        return new StaffAccountPrincipal(
                account.getId(),
                account.getUsername(),
                account.getPasswordHash(),
                storeId,
                account.isEnabled(),
                storeEnabled
        );
    }
}

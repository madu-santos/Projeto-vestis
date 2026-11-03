package com.clothingrental.auth.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Representa o funcionário autenticado no contexto de segurança da aplicação.
 *
 * <p>Disponibiliza o identificador da loja ({@code storeId}) associada ao usuário
 * logado, garantindo o isolamento multi-tenant das operações administrativas.
 */
public class StaffAccountPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final Long storeId;
    private final boolean enabled;
    private final boolean storeEnabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public StaffAccountPrincipal(Long id, String username, String passwordHash, Long storeId, boolean enabled) {
        this(id, username, passwordHash, storeId, enabled, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    public StaffAccountPrincipal(Long id, String username, String passwordHash, Long storeId,
                                 boolean enabled, boolean storeEnabled) {
        this(id, username, passwordHash, storeId, enabled, storeEnabled,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    public StaffAccountPrincipal(Long id, String username, String passwordHash, Long storeId,
                                 boolean enabled, boolean storeEnabled,
                                 Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "O username é obrigatório.");
        this.passwordHash = Objects.requireNonNull(passwordHash, "O hash de senha é obrigatório.");
        this.storeId = storeId;
        this.enabled = enabled;
        this.storeEnabled = storeEnabled;
        this.authorities = authorities == null ? Collections.emptyList() : Collections.unmodifiableCollection(authorities);
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public boolean isStoreEnabled() {
        return storeEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return storeEnabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled && storeEnabled;
    }
}

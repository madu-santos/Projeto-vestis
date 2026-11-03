package com.clothingrental.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Conta de um funcionário com acesso ao painel administrativo.
 *
 * <p>É apenas uma entidade de persistência: a conversão para o formato que o
 * Spring Security entende fica em {@code StaffAccountDetailsService}.
 */
@Entity
@Table(name = "staff_accounts")
public class StaffAccount {

    public static final int USERNAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = USERNAME_MAX_LENGTH)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Exigido pelo JPA; não deve ser usado pelo código da aplicação. */
    protected StaffAccount() {
    }

    /**
     * Cria uma conta ativa.
     *
     * @param passwordHash senha já codificada (BCrypt), nunca a senha em texto puro
     */
    public StaffAccount(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package com.clothingrental.auth.model;

import com.clothingrental.tenant.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Conta de um funcionário com acesso ao painel administrativo de uma loja (tenant).
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

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
     * Cria uma conta ativa associada obrigatoriamente a um estabelecimento.
     *
     * @param passwordHash senha já codificada (BCrypt), nunca a senha em texto puro
     * @param store estabelecimento ao qual o funcionário pertence
     */
    public StaffAccount(String username, String passwordHash, Store store) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.store = Objects.requireNonNull(store, "O estabelecimento da conta administrativa é obrigatório.");
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
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

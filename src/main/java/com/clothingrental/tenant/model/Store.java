package com.clothingrental.tenant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Representa um estabelecimento (loja/tenant) na plataforma Vestis.
 *
 * <p>Todas as operações de catálogo, solicitações e locações pertencem
 * a uma loja identificada pelo seu {@code id}.
 */
@Entity
@Table(name = "stores")
public class Store {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int SLUG_MAX_LENGTH = 100;

    /**
     * O slug deve conter apenas letras minúsculas, números e hífens,
     * iniciando e terminando com caractere alfanumérico.
     */
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, unique = true, length = SLUG_MAX_LENGTH)
    private String slug;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Construtor exigido pelo JPA; não deve ser chamado pelo código da aplicação. */
    protected Store() {
    }

    /**
     * Cria uma nova loja ativa com as informações essenciais.
     */
    public Store(String name, String slug) {
        this(name, slug, null, null, null, null);
    }

    public Store(String name, String slug, String logoPath, String contactEmail, String contactPhone, String address) {
        this.name = validateName(name);
        this.slug = validateSlug(slug);
        this.logoPath = normalizeOptionalText(logoPath);
        this.contactEmail = normalizeOptionalText(contactEmail);
        this.contactPhone = normalizeOptionalText(contactPhone);
        this.address = normalizeOptionalText(address);
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do estabelecimento é obrigatório.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O nome do estabelecimento deve ter no máximo " + NAME_MAX_LENGTH + " caracteres.");
        }
        return trimmed;
    }

    public static String validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("O slug do estabelecimento é obrigatório.");
        }
        String trimmed = slug.trim();
        if (trimmed.length() > SLUG_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O slug do estabelecimento deve ter no máximo " + SLUG_MAX_LENGTH + " caracteres.");
        }
        if (!SLUG_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "O slug deve conter apenas letras minúsculas, números e hífens (exemplo: 'loja-aurora').");
        }
        return trimmed;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public void updateBasicInfo(String name, String logoPath, String contactEmail, String contactPhone, String address) {
        this.name = validateName(name);
        this.logoPath = normalizeOptionalText(logoPath);
        this.contactEmail = normalizeOptionalText(contactEmail);
        this.contactPhone = normalizeOptionalText(contactPhone);
        this.address = normalizeOptionalText(address);
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return id != null && Objects.equals(id, store.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

package com.clothingrental.auth.security;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.repository.StoreRepository;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/**
 * Validador de autorização em tempo real para as rotas administrativas (/admin/**).
 *
 * <p>A cada requisição administrativa, confere não apenas se o funcionário possui a
 * role ADMIN, mas também se o estabelecimento (tenant) ao qual sua conta pertence
 * permanece ativo no banco PostgreSQL. Impede que sessões abertas continuem operando
 * caso a loja seja desativada.
 */
@Component
public class AdminStoreActiveAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final StoreRepository storeRepository;

    public AdminStoreActiveAuthorizationManager(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();

        // 1. Verifica se está autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // 2. Verifica se possui a role ROLE_ADMIN
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!hasAdminRole) {
            return new AuthorizationDecision(false);
        }

        // 3. Obtém o storeId do StaffAccountPrincipal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof StaffAccountPrincipal staffPrincipal)) {
            return new AuthorizationDecision(false);
        }

        Long storeId = staffPrincipal.getStoreId();
        if (storeId == null) {
            return new AuthorizationDecision(false);
        }

        // 4. Consulta o PostgreSQL em tempo real: a loja continua ativa no momento da requisição?
        boolean isStoreActive = storeRepository.existsByIdAndEnabledTrue(storeId);

        // 5 & 6. Permite somente se a loja estiver ativa; nega (HTTP 403) se estiver inativa ou inexistente
        return new AuthorizationDecision(isStoreActive);
    }
}

package com.clothingrental.tenant.service;

import com.clothingrental.auth.model.StaffAccountPrincipal;
import com.clothingrental.tenant.exception.NoCurrentStoreContextException;
import com.clothingrental.tenant.exception.StoreDisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por identificar a loja (tenant) do funcionário autenticado.
 *
 * <p>Obtém o {@code storeId} exclusivamente a partir do {@link StaffAccountPrincipal}
 * armazenado no contexto de segurança gerenciado pelo Spring Security.
 * Impede que o frontend ou parâmetros de requisição ditem arbitrariamente a loja da operação.
 */
@Service
public class CurrentStoreService {

    /**
     * Retorna o ID da loja associada ao funcionário autenticado no momento.
     *
     * @return identificador numérico da loja (store_id)
     * @throws NoCurrentStoreContextException se não houver usuário autenticado ou se o principal não contiver loja
     * @throws StoreDisabledException se a loja associada à conta estiver inativa
     */
    public Long getCurrentStoreId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NoCurrentStoreContextException("Operação administrativa requer autenticação prévia.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof StaffAccountPrincipal staffPrincipal) {
            Long storeId = staffPrincipal.getStoreId();
            if (storeId == null) {
                throw new NoCurrentStoreContextException("A conta autenticada não possui uma loja associada.");
            }
            if (!staffPrincipal.isStoreEnabled()) {
                throw new StoreDisabledException("A loja associada a esta conta administrativa está desativada.");
            }
            return storeId;
        }

        throw new NoCurrentStoreContextException(
                "O usuário autenticado não é uma conta administrativa com contexto de loja.");
    }
}

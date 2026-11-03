package com.clothingrental.tenant.exception;

import com.clothingrental.shared.exception.BusinessException;

/**
 * Exceção lançada quando uma operação administrativa é solicitada sem um contexto
 * válido de estabelecimento (usuário não autenticado ou sem loja associada).
 */
public class NoCurrentStoreContextException extends BusinessException {

    public NoCurrentStoreContextException() {
        super("Nenhum estabelecimento identificado no contexto de autenticação atual.");
    }

    public NoCurrentStoreContextException(String message) {
        super(message);
    }
}

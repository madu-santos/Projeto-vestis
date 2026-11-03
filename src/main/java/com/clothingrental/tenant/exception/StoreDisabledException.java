package com.clothingrental.tenant.exception;

import com.clothingrental.shared.exception.BusinessException;

/**
 * Exceção lançada quando uma loja está inativa ou desativada no sistema.
 */
public class StoreDisabledException extends BusinessException {

    public StoreDisabledException(String slug) {
        super("O estabelecimento '" + slug + "' encontra-se temporariamente desativado.");
    }
}

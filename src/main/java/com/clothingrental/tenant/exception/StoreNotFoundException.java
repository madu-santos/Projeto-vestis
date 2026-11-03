package com.clothingrental.tenant.exception;

import com.clothingrental.shared.exception.ResourceNotFoundException;

/**
 * Exceção lançada quando uma loja não é encontrada pelo seu identificador ou slug.
 */
public class StoreNotFoundException extends ResourceNotFoundException {

    public StoreNotFoundException(String slug) {
        super("Estabelecimento não encontrado para o identificador: " + slug);
    }
}

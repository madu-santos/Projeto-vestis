package com.clothingrental.shared.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado no sistema.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

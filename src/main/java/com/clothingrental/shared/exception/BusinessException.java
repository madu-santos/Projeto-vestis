package com.clothingrental.shared.exception;

/**
 * Exceção base para regras de negócio e validações da aplicação.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

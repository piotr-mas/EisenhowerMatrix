package com.piotr.matrix.auth.exception;

public class TlsException extends RuntimeException{

    private final String errorMessage;

    public TlsException(String message) {
        this.errorMessage = message;
    }
}

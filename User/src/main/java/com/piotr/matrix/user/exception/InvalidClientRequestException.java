package com.piotr.matrix.user.exception;

public class InvalidClientRequestException extends RuntimeException {
    public InvalidClientRequestException(String message) {
        super(message);
    }
}

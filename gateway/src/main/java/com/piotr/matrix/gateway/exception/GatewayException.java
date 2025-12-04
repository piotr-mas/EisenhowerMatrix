package com.piotr.matrix.gateway.exception;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {
    private final int code;
    public GatewayException(String message) {
        super(message);
        this.code = 500;
    }
    public GatewayException(int code, String message) {
        super(message);
        this.code = code;
    }
}

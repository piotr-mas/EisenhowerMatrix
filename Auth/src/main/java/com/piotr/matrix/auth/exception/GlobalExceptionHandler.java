package com.piotr.matrix.auth.exception;

import com.piotr.matrix.auth.generated.model.AuthError;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<AuthError> handleAuthException(DisabledException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(403).body(new AuthError("403", e.getMessage()));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<AuthError> handleAuthException(InternalAuthenticationServiceException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(404).body(new AuthError("404", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthError> handleAuthException(BadCredentialsException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(401).body(new AuthError("401", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthError> handleAuthException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(500).body(new AuthError("500", e.getMessage()));
    }
}

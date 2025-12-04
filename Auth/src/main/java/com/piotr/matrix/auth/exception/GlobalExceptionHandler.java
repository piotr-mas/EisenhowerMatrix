package com.piotr.matrix.auth.exception;


import com.piotr.matrix.generated.model.MatrixError;
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MatrixError> handleUserNotFound(UserNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(404).body(new MatrixError("404", ex.getMessage()));
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<MatrixError> handleAuthException(DisabledException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(403).body(new MatrixError("403", e.getMessage()));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<MatrixError> handleAuthException(InternalAuthenticationServiceException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(404).body(new MatrixError("404", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MatrixError> handleAuthException(BadCredentialsException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(401).body(new MatrixError("401", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MatrixError> handleAuthException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(500).body(new MatrixError("500", e.getMessage()));
    }
}

package com.piotr.matrix.user.exception;

import com.piotr.matrix.generated.model.MatrixError;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MatrixError> handleUserNotFoundException(UserNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(404).body(new MatrixError("404", e.getMessage()));
    }

    @ExceptionHandler(InvalidClientRequestException.class)
    public ResponseEntity<MatrixError> handleUserNotFoundException(InvalidClientRequestException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(400).body(new MatrixError("400", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MatrixError> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolation captured: {}", e.getMessage());
        String detailMessage = e.getMessage().substring(e.getMessage().indexOf("[")+1, e.getMessage().indexOf("]"));
        // Use 409 Conflict for resource creation failures due to existing data
        return ResponseEntity.status(409).body(new MatrixError("409", detailMessage));
    }
}

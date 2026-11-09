package com.example.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class PerformanceDatabaseExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PerformanceDatabaseExceptionHandler.class);

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<DatabaseError> handleDatabaseFailure(SQLException exception) {
        LOGGER.error("Local performance PostgreSQL request failed", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new DatabaseError("Performance database unavailable"));
    }

    public record DatabaseError(String error) {
    }
}

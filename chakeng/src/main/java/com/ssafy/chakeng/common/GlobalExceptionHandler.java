package com.ssafy.chakeng.common;

import com.ssafy.chakeng.report.exception.DuplicateReportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCred(BadCredentialsException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(401)
                .body(ApiResponse.error("401", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(400)
                .body(ApiResponse.error("400", "나쁜 응답이라능"));
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateReportException ex) {
        return ResponseEntity.status(409).body(ApiResponse.error("409",ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(ApiResponse.error("409", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(409).body(ApiResponse.error("409", e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(500)
                .body(ApiResponse.error("500", e.getMessage()));
    }
}

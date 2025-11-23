package com.trading.system.application.exception;

import com.trading.system.application.dto.ErrorResponse;
import com.trading.system.domain.exception.ConcurrentTradeException;
import com.trading.system.domain.exception.InsufficientBalanceException;
import com.trading.system.domain.exception.PriceNotFoundException;
import com.trading.system.domain.exception.StalePriceException;
import com.trading.system.domain.exception.UnsupportedSymbolException;
import com.trading.system.domain.exception.WalletNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.error("Insufficient balance: {}", ex.getMessage());
        var error = new ErrorResponse(
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        log.error("Wallet not found: {}", ex.getMessage());
        var error = new ErrorResponse(
                "WALLET_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFound(PriceNotFoundException ex) {
        log.error("Price not found: {}", ex.getMessage());
        var error = new ErrorResponse(
                "PRICE_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnsupportedSymbolException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedSymbol(UnsupportedSymbolException ex) {
        log.error("Unsupported symbol: {}", ex.getMessage());
        var error = new ErrorResponse(
                "UNSUPPORTED_SYMBOL",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(StalePriceException.class)
    public ResponseEntity<ErrorResponse> handleStalePrice(StalePriceException ex) {
        log.error("Stale price detected: {}", ex.getMessage());
        var error = new ErrorResponse(
                "STALE_PRICE",
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, ConcurrentTradeException.class })
    public ResponseEntity<ErrorResponse> handleConcurrentTrade(Exception ex) {
        log.warn("Concurrent trade detected: {}", ex.getMessage());
        var error = new ErrorResponse(
                "CONCURRENT_TRADE",
                "Another trade is being processed on this wallet. Please try again.",
                HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Wrap exception from spring validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error("Validation failed: {}", errors);
        var error = new ErrorResponse(
                "VALIDATION_FAILED",
                errors,
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        var message = ex.getMessage();
        var errorMessage = "Invalid request format";

        if (message != null && message.contains("Cannot deserialize value")) {
            if (message.contains("OrderAction")) {
                errorMessage = "Invalid order action. Allowed values: BUY, SELL";
            } else {
                errorMessage = "Invalid value in request. Please check the allowed values.";
            }
        }

        log.error("Message not readable: {}", message);
        var error = new ErrorResponse(
                "INVALID_REQUEST_FORMAT",
                errorMessage,
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        var error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

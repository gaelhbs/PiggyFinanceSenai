package com.piggy.piggyfinance.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super("Amount must be greater than zero.");
    }
}

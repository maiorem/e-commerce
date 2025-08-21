package com.loopers.support.error;

public class InsufficientPointException extends RuntimeException{

    public InsufficientPointException(String message) {
        super(message);
    }

    public InsufficientPointException(String message, Throwable cause) {
        super(message, cause);
    }
}

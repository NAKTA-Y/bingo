package com.nhnacademy.exception;

public class AlreadySelectedException extends RuntimeException {
    public AlreadySelectedException(String message) {
        super(message);
    }
}
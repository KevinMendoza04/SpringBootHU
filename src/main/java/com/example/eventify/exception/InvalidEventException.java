package com.example.eventify.exception;

public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String mensaje) {
        super(mensaje);
    }
}
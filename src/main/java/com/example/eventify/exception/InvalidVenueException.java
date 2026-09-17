package com.example.eventify.exception;

public class InvalidVenueException extends RuntimeException {

    public InvalidVenueException(String mensaje) {
        super(mensaje);
    }
}
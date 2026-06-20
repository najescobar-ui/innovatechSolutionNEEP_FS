package com.duoc.resources.exception;

/** Recurso de negocio inexistente; el handler global la mapea a 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

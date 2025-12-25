package com.locus.projectlocusprototype.Exceptions;

public class InvalidNoteRequestException extends RuntimeException {
    public InvalidNoteRequestException(String message) {
        super(message);
    }
}

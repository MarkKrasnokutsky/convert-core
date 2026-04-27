package com.mark.convert.core.exception;

public class ManageFileException extends RuntimeException {
    public ManageFileException(String message) {
        super(message);
    }
    public ManageFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

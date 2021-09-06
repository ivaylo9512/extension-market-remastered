package com.tick42.quicksilver.exceptions;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String exception) {
        super(exception);
    }
}

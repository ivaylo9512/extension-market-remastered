package com.tick42.quicksilver.exceptions;

public class DisabledUserException extends RuntimeException{
    public DisabledUserException(String message){
        super(message);
    }
}

package com.amigo.secreto.services.exceptions;

public class DrawAlreadyDoneException extends RuntimeException{

    public DrawAlreadyDoneException(String message) {
        super(message);
    }
}

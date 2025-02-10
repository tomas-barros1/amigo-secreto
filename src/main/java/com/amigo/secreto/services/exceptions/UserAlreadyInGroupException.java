package com.amigo.secreto.services.exceptions;

public class UserAlreadyInGroupException extends RuntimeException {

    public UserAlreadyInGroupException(String message) {
        super(message);
    }
}

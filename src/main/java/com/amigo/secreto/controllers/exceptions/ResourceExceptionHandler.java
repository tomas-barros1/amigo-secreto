package com.amigo.secreto.controllers.exceptions;

import com.amigo.secreto.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.time.Instant;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseException> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request){
        BaseException resourceNotFound = BaseException.builder().
                error("Resource not found")
                .path(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(resourceNotFound.getStatus()).body(resourceNotFound);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseException> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        BaseException dataIntegrityViolation = BaseException.builder().
                error("Data integrity violation")
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(dataIntegrityViolation.getStatus()).body(dataIntegrityViolation);
    }

    @ExceptionHandler(UserAlreadyInGroupException.class)
    public ResponseEntity<BaseException> handleUserAlreadyInGroup(UserAlreadyInGroupException e, HttpServletRequest request) {
        BaseException userAlreadyInGroup = BaseException.builder().
                error("User already in group")
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(userAlreadyInGroup.getStatus()).body(userAlreadyInGroup);
    }

    @ExceptionHandler(DrawAlreadyDoneException.class)
    public ResponseEntity<BaseException> handleDrawAlreadyDone(DrawAlreadyDoneException e, HttpServletRequest request) {
        BaseException drawAlreadyDone = BaseException.builder().
                error("Draw already done")
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(drawAlreadyDone.getStatus()).body(drawAlreadyDone);
    }

    @ExceptionHandler(DrawPairNumberException.class)
    public ResponseEntity<BaseException> handleDrawPairNumber(DrawPairNumberException e, HttpServletRequest request) {
        BaseException drawPairNumber = BaseException.builder().
                error("O sorteio só pode ser realizado em números pares de participantes")
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(drawPairNumber.getStatus()).body(drawPairNumber);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<BaseException> handleSqlException(SQLException e, HttpServletRequest request) {
        BaseException sqlException = BaseException.builder().
                error("Erro no banco de dados")
                .path(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(sqlException.getStatus()).body(sqlException);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseException> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        BaseException badCredentials = BaseException.builder()
                .error("Credenciais inválidas")
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(badCredentials.getStatus()).body(badCredentials);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseException> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        BaseException illegalArgument = BaseException.builder()
                .error("Argumento inválido")
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(illegalArgument.getStatus()).body(illegalArgument);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseException> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        BaseException accessDenied = BaseException.builder()
                .error("Acesso negado")
                .path(request.getRequestURI())
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(accessDenied.getStatus()).body(accessDenied);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<BaseException> handleForbidden(ForbiddenException e, HttpServletRequest request) {
        BaseException exception = BaseException.builder()
                .error("Acesso proibido")
                .path(request.getRequestURI())
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(exception.getStatus()).body(exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseException> handleGenericException(Exception e, HttpServletRequest request) {
        BaseException exception = BaseException.builder()
                .error("Erro interno do servidor")
                .path(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(exception.getStatus()).body(exception);
    }
}
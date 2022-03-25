package com.example.signrequestusingawssignature.handlers.exceptions;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ValidationException {
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDTO> handleValidationExceptions(
	  MethodArgumentNotValidException ex) {
		 ErrorDTO error = new ErrorDTO("MethodArgumentNotValidException", Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage());
		 return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}

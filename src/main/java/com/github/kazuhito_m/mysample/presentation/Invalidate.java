package com.github.kazuhito_m.mysample.presentation;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.seasar.doma.jdbc.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

/**
 * Validationでerrorとなった場合のResponseBody。
 */
public class Invalidate {
    final LocalDateTime timestamp = LocalDateTime.now();
    final int status;
    final String error;
    final String errorCause;

    public Invalidate(BindException e, HttpStatus httpStatus) {
        status = httpStatus.value();
        error = httpStatus.getReasonPhrase();
        errorCause = errorCauseOf(e.getFieldError());
    }

    private static String errorCauseOf(FieldError invalid) {
        String message = invalid.getDefaultMessage();
        // 型がFieldError以外の場合、自前で用意したValidationに引っかかってる…ということはFieldErrorなら「型変換失敗」なので、ぼやかす。
        if (invalid.getClass().getName().contains("FieldError")) message = "書式が正しくありません。";
        String fieldName = invalid.getField();
        return String.format("%s [%s:'%s']", message, fieldName, invalid.getRejectedValue());
    }


    public Invalidate(MethodArgumentNotValidException e, HttpStatus httpStatus) {
        status = httpStatus.value();
        error = httpStatus.getReasonPhrase();
        errorCause = errorCauseOf(e.getBindingResult().getFieldError());
    }

    public Invalidate(HttpMessageNotReadableException e, HttpStatus httpStatus) {
        status = httpStatus.value();
        error = httpStatus.getReasonPhrase();
        errorCause = errorCauseOf(e);
    }

    private String errorCauseOf(HttpMessageNotReadableException e) {
        if (!(e.getCause() instanceof InvalidFormatException)) return "書式が正しくありません。";

        InvalidFormatException ife = (InvalidFormatException) e.getCause();
        JsonMappingException.Reference firstRef = ife.getPath().get(0);
        String message = "書式が正しくありません。";
        String fieldName = firstRef.getFieldName();
        String value = ife.getValue().toString();
        return String.format("%s [%s:'%s']", message, fieldName, value);
    }

    public Invalidate(UniqueConstraintException e, HttpStatus httpStatus) {
        status = httpStatus.value();
        error = httpStatus.getReasonPhrase();
        errorCause = "指定されたデータはすでに存在しています。";
    }
}

package com.zinkworks.atmapplication.common.rest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class RestException extends RuntimeException {

    private final String errorMessage;
    private final HttpStatus httpStatus;

    public RestException(final String errorMessage, final HttpStatus httpStatus) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }


}

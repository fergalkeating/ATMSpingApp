package com.zinkworks.atmapplication.common.rest.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RestException{

    public BadRequestException(final String errorMessage)
    {
        super(errorMessage, HttpStatus.BAD_REQUEST);
    }
}

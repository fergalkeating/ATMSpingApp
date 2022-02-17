package com.zinkworks.atmapplication.common.rest.exception;

public class RequestedFundsNotAvailableException extends BadRequestException {

    public RequestedFundsNotAvailableException() {
        super("The amount requested exceeds the available balance of your account");
    }
}

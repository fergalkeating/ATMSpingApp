package com.zinkworks.atmapplication.common.rest.exception;

public class BalanceNotAvailableAtATMException extends BadRequestException {

    public BalanceNotAvailableAtATMException() {
        super("The balance requested is not available at this ATM. Please try a different amount in multiples of 5, 10, 20 and 50.");
    }
}

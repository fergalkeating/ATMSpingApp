package com.zinkworks.atmapplication.common.rest.exception;

import java.text.MessageFormat;

public class AccountDoesNotMatchAuthorizationException extends BadRequestException {

    public AccountDoesNotMatchAuthorizationException(String accountId) {
        super(MessageFormat.format("The user provided does not have access to an account with accountId {0}. ", accountId));
    }
}

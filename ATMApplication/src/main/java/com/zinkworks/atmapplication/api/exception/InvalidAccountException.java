package com.zinkworks.atmapplication.api.exception;

import com.zinkworks.atmapplication.common.rest.exception.BadRequestException;

import java.text.MessageFormat;

public class InvalidAccountException extends BadRequestException {

    public InvalidAccountException(final String accountId)
    {
        //This is the same returned error as AccountDoesNotMatchAuthorizationException to prevent phishing for account Ids
        super(MessageFormat.format("The user provided does not have access to an account with accountId {0}.", accountId));
    }
}

package com.zinkworks.atmapplication.api;

import com.zinkworks.atmapplication.api.exception.InvalidAccountException;
import com.zinkworks.atmapplication.service.atm.account.AccountService;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceResponseDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;

@ExtendWith(SpringExtension.class)
class ATMEndpointTest {

    private static final String ACCOUNT_BALANCE = "300";
    private static final int WITHDRAW_AMOUNT_20 = 20;
    private static final String ACCOUNT_ID = "1234";

    @Mock
    AccountService accountService;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @InjectMocks
    ATMEndpoint atmEndpoint;

    @Test
    public void withdrawFromAccountTest_200Response() {

        WithdrawFromAccountResponseDto withdrawFromAccountResponseDto =
                WithdrawFromAccountResponseDto.builder()
                        .accountId(ACCOUNT_ID)
                        .amountWithdrawn(String.valueOf(WITHDRAW_AMOUNT_20))
                        .build();


        Mockito.when(accountService.withdrawFromAccount(any(WithdrawFromAccountRequestDto.class))).thenReturn(withdrawFromAccountResponseDto);

        ResponseEntity response = atmEndpoint.withdrawFromAccount(ACCOUNT_ID, String.valueOf(WITHDRAW_AMOUNT_20));

        assertEquals(MessageFormat.format("You have withdrawn {0}.", WITHDRAW_AMOUNT_20), response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getBalanceTest_200Response() {

        Mockito.when(authentication.getName()).thenReturn(ACCOUNT_ID);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        RetrieveAccountBalanceResponseDto retrieveAccountBalanceResponseDto =
                RetrieveAccountBalanceResponseDto
                        .builder()
                        .accountBalance(ACCOUNT_BALANCE)
                        .build();

        Mockito.when(accountService.getAccountBalance(any(RetrieveAccountBalanceRequestDto.class))).thenReturn(retrieveAccountBalanceResponseDto);

        ResponseEntity response = atmEndpoint.getBalance(ACCOUNT_ID);

        assertEquals(MessageFormat.format("You have a balance of {0}.", ACCOUNT_BALANCE), response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void getNullBalanceTest_ExceptionResponse() {

        Mockito.when(authentication.getName()).thenReturn(ACCOUNT_ID);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(accountService.getAccountBalance(any(RetrieveAccountBalanceRequestDto.class))).thenReturn(null);


        final InvalidAccountException exception = assertThrows(InvalidAccountException.class, () ->
                atmEndpoint.getBalance(ACCOUNT_ID));

        assertEquals(MessageFormat.format("The user provided does not have access to an account with accountId accountId.", ACCOUNT_BALANCE), exception.getErrorMessage());
    }
}

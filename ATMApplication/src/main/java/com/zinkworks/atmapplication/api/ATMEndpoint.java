package com.zinkworks.atmapplication.api;

import com.zinkworks.atmapplication.api.exception.InvalidAccountException;
import com.zinkworks.atmapplication.common.rest.exception.AccountDoesNotMatchAuthorizationException;
import com.zinkworks.atmapplication.service.atm.account.AccountService;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceResponseDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;

@Controller
@Slf4j
@RequestMapping(path = "/atm")
public class ATMEndpoint {

    private final AccountService accountService;

    Authentication auth;

    public ATMEndpoint(final AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping(path = "/account/{accountId}/withdraw")
    public @ResponseBody
    ResponseEntity withdrawFromAccount(
            @PathVariable(name = "accountId") String accountId,
            @RequestParam(name = "amount") String amount
    ) {
        log.info(String.format("Received Withdraw Balance Request of amount %s", amount));
        WithdrawFromAccountRequestDto withdrawFromAccountRequestDto = WithdrawFromAccountRequestDto.builder()
                .accountId(accountId)
                .withdrawalAmount(Integer.parseInt(amount))
                .build();

        WithdrawFromAccountResponseDto withdrawFromAccountResponseDto = accountService.withdrawFromAccount(withdrawFromAccountRequestDto);
        //Todo Once API confirmed, create API request / response objects instead of using DTO in response.
        //TODO Also Add Versioning to API.
        return ResponseEntity.ok(withdrawFromAccountResponseDto);
    }


    @GetMapping(path = "/account/{accountId}")
    public @ResponseBody
    ResponseEntity getBalance(@PathVariable(required = true) String accountId) {

        auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("received balance request for account: {}", accountId);

        if (!accountId.equals(auth.getName())) {
            throw new AccountDoesNotMatchAuthorizationException(accountId);
        }

        RetrieveAccountBalanceRequestDto retrieveAccountBalanceRequestDto = RetrieveAccountBalanceRequestDto.builder()
                .accountId(accountId)
                .build();

        RetrieveAccountBalanceResponseDto responseDto = accountService.getAccountBalance(retrieveAccountBalanceRequestDto);

        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        } else {
            throw new InvalidAccountException("accountId");
        }
    }

}



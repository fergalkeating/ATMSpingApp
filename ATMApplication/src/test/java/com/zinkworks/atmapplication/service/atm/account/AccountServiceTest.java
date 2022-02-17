package com.zinkworks.atmapplication.service.atm.account;

import com.zinkworks.atmapplication.api.exception.InvalidAccountException;
import com.zinkworks.atmapplication.common.rest.exception.BalanceNotAvailableAtATMException;
import com.zinkworks.atmapplication.common.rest.exception.RequestedFundsNotAvailableException;
import com.zinkworks.atmapplication.common.rest.exception.UnexpectedRuntimeException;
import com.zinkworks.atmapplication.model.atm.ATM;
import com.zinkworks.atmapplication.model.atm.Currency;
import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import com.zinkworks.atmapplication.repository.atm.ATMRepository;
import com.zinkworks.atmapplication.repository.atm.CurrencyRepository;
import com.zinkworks.atmapplication.repository.customer.CustomerAccountRepository;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceResponseDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class AccountServiceTest {

    private static final int ACCOUNT_BALANCE = 300;
    private static final int WITHDRAW_AMOUNT_20 = 20;
    private static final String ACCOUNT_ID = "1234";

    @Mock
    CustomerAccountRepository customerAccountRepository;

    @Mock
    ATMRepository atmRepository;

    @Mock
    CurrencyRepository currencyRepository;

    @InjectMocks
    AccountService accountService;

    ATM atm;
    List<Currency> currencyList;


    private void initialiseData() {

        currencyList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            currencyList.add(Currency.builder().currency("EURO").name("TWENTY").value(20).build());
        }
        for (int i = 0; i < 10; i++) {
            currencyList.add(Currency.builder().currency("EURO").name("FIFTY").value(50).build());
        }
        for (int i = 0; i < 30; i++) {
            currencyList.add(Currency.builder().currency("EURO").name("TEN").value(10).build());
        }
        for (int i = 0; i < 20; i++) {
            currencyList.add(Currency.builder().currency("EURO").name("FIVE").value(5).build());
        }

        atm = ATM.
                builder()
                .ATMBalance("1500")
                .ATMName("OnlyATM")
                .currency(currencyList)
                .build();
    }

    private void initialiseAtmWithOneFiftyEuro() {

        currencyList = new ArrayList<>();
        currencyList.add(Currency.builder().currency("EURO").name("FIFTY").value(50).build());

        atm = ATM.
                builder()
                .ATMBalance("50")
                .ATMName("OnlyATM")
                .currency(currencyList)
                .build();
    }

    @Test
    void getAccountBalance_ValidResponse() {
        CustomerAccount customerAccount =
                CustomerAccount.builder()
                        .accountBalance(ACCOUNT_BALANCE)
                        .accountId(ACCOUNT_ID)
                        .build();

        Mockito.when(customerAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(customerAccount));

        RetrieveAccountBalanceRequestDto retrieveAccountBalanceRequestDto =
                RetrieveAccountBalanceRequestDto.builder()
                        .accountId(ACCOUNT_ID).build();
        RetrieveAccountBalanceResponseDto response = accountService.getAccountBalance(retrieveAccountBalanceRequestDto);

        assertEquals(String.valueOf(ACCOUNT_BALANCE), response.getAccountBalance());
    }

    @Test
    void getAccountBalance_NullResponse() {
        CustomerAccount customerAccount =
                CustomerAccount.builder()
                        .accountBalance(ACCOUNT_BALANCE)
                        .accountId(ACCOUNT_ID)
                        .build();

        Mockito.when(customerAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(null));

        RetrieveAccountBalanceRequestDto retrieveAccountBalanceRequestDto =
                RetrieveAccountBalanceRequestDto.builder()
                        .accountId(ACCOUNT_ID).build();
        RetrieveAccountBalanceResponseDto response = accountService.getAccountBalance(retrieveAccountBalanceRequestDto);

        assertNull(response);
    }

    @Test
    void testGetAccountBalance_WithAccountId() {
        CustomerAccount customerAccount =
                CustomerAccount.builder()
                        .accountBalance(ACCOUNT_BALANCE)
                        .accountId(ACCOUNT_ID)
                        .build();

        Mockito.when(customerAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(customerAccount));
        CustomerAccount customerAccountResponse = accountService.getAccountBalance(ACCOUNT_ID);
        assertEquals(customerAccountResponse, customerAccount);
    }

    @Test
    void testWithdrawFromAccount_ValidResponse() {

        initialiseData();
        createMockResponses();

        WithdrawFromAccountRequestDto withdrawFromAccountRequestDto
                = WithdrawFromAccountRequestDto.
                builder()
                .withdrawalAmount(20)
                .accountId(ACCOUNT_ID)
                .build();


        WithdrawFromAccountResponseDto responseDto = accountService.withdrawFromAccount(withdrawFromAccountRequestDto);

        assertEquals(responseDto.getAccountId(), ACCOUNT_ID);
        assertEquals(responseDto.getAmountWithdrawn(), "20");
    }

    @Test
    void testWithdrawFromAccount_RequestedFundsNotAvailableException() {

        initialiseData();
        createMockResponses();

        WithdrawFromAccountRequestDto withdrawFromAccountRequestDto
                = WithdrawFromAccountRequestDto.
                builder()
                .withdrawalAmount(500)
                .accountId(ACCOUNT_ID)
                .build();

        final RequestedFundsNotAvailableException exception = assertThrows(RequestedFundsNotAvailableException.class, () ->
                accountService.withdrawFromAccount(withdrawFromAccountRequestDto));

        assertEquals("The amount requested exceeds the available balance of your account", exception.getErrorMessage());
    }

    @Test
    void testWithdrawFromAccount_BalanceNotAvailableAtATMException() {

        initialiseAtmWithOneFiftyEuro();
        createMockResponses();

        WithdrawFromAccountRequestDto withdrawFromAccountRequestDto
                = WithdrawFromAccountRequestDto.
                builder()
                .withdrawalAmount(100)
                .accountId(ACCOUNT_ID)
                .build();

        final BalanceNotAvailableAtATMException exception = assertThrows(BalanceNotAvailableAtATMException.class, () ->
                accountService.withdrawFromAccount(withdrawFromAccountRequestDto));

        assertEquals("The balance requested is not available at this ATM. Please try a different amount in multiples of 5, 10, 20 and 50.", exception.getErrorMessage());
    }

    @Test
    void testWithdrawFromAccount_MissingAcount_RuntimeException() {

        initialiseAtmWithOneFiftyEuro();
        createMockResponses();
        Mockito.when(customerAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
        WithdrawFromAccountRequestDto withdrawFromAccountRequestDto
                = WithdrawFromAccountRequestDto.
                builder()
                .withdrawalAmount(100)
                .accountId(ACCOUNT_ID)
                .build();

        final UnexpectedRuntimeException exception = assertThrows(UnexpectedRuntimeException.class, () ->
                accountService.withdrawFromAccount(withdrawFromAccountRequestDto));

        assertEquals("Unexpected state. Account should exist if already validated on Request.", exception.getMessage());
    }

    private void createMockResponses() {
        CustomerAccount customerAccount =
                CustomerAccount.builder()
                        .accountBalance(ACCOUNT_BALANCE)
                        .accountId(ACCOUNT_ID)
                        .build();

        Mockito.when(customerAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(customerAccount));

        Mockito.when(atmRepository.findById(1L)).thenReturn(Optional.of(atm));
        Mockito.when(currencyRepository.findByAtm(atm, Sort.by(Sort.Direction.ASC, "value"))).thenReturn(currencyList);

    }


}

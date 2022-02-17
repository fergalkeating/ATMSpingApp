package com.zinkworks.atmapplication.service.atm.account;

import com.zinkworks.atmapplication.common.rest.exception.BalanceNotAvailableAtATMException;
import com.zinkworks.atmapplication.common.rest.exception.RequestedFundsNotAvailableException;
import com.zinkworks.atmapplication.common.rest.exception.UnexpectedRuntimeException;
import com.zinkworks.atmapplication.model.atm.ATM;
import com.zinkworks.atmapplication.model.atm.Currency;
import com.zinkworks.atmapplication.repository.atm.CurrencyRepository;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceRequestDto;
import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import com.zinkworks.atmapplication.repository.atm.ATMRepository;
import com.zinkworks.atmapplication.repository.customer.CustomerAccountRepository;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceResponseDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountRequestDto;
import com.zinkworks.atmapplication.service.atm.dto.WithdrawFromAccountResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ATMRepository atmRepository;
    @Autowired
    CustomerAccountRepository customerAccountRepository;

    public RetrieveAccountBalanceResponseDto getAccountBalance(RetrieveAccountBalanceRequestDto retrieveAccountBalanceRequestDto) {
        log.info("Retrieving database account " + retrieveAccountBalanceRequestDto.getAccountId());
        Optional<CustomerAccount> customerAccount = customerAccountRepository.findByAccountId(retrieveAccountBalanceRequestDto.getAccountId());
        return customerAccount.map(account -> RetrieveAccountBalanceResponseDto.builder().accountBalance(String.valueOf(account.getAccountBalance())).build()).orElse(null);
    }

    @Transactional(readOnly = true)
    public CustomerAccount getAccountBalance(String accountId) {
        log.info("getting account " + accountId);
        Optional<CustomerAccount> customerAccount = customerAccountRepository.findByAccountId(accountId);
        if (customerAccount.isPresent()) {
            log.info("Retrieved Account " + accountId);
            return customerAccount.get();
        }

        return null;
    }

    @Transactional
    public WithdrawFromAccountResponseDto withdrawFromAccount(WithdrawFromAccountRequestDto withdrawFromAccountRequestDto) {
        String accountId = withdrawFromAccountRequestDto.getAccountId();
        Integer withdrawalAmountRequested = withdrawFromAccountRequestDto.getWithdrawalAmount();

        Optional<CustomerAccount> customerAccountOptional = customerAccountRepository.findByAccountId(accountId);

        Integer customerBalance = validateCustomersAccountAndBalance(customerAccountOptional, withdrawalAmountRequested);

        List<Currency> currency = getAvailableCurrency();
        List<Long> extractedMoneyList = withdrawMoney(currency, withdrawalAmountRequested);

        if (extractedMoneyList == null) {
            throw new BalanceNotAvailableAtATMException();
        }

        currencyRepository.deleteAllById(extractedMoneyList);

        CustomerAccount accountToUpdate;
        if(customerAccountOptional.isPresent()) {
            accountToUpdate = customerAccountOptional.get();
        }
        else
            {throw new UnexpectedRuntimeException("");}
        accountToUpdate.setAccountBalance(customerBalance - withdrawalAmountRequested);
        customerAccountRepository.save(accountToUpdate);

        return WithdrawFromAccountResponseDto.builder()
                .accountId(accountId).
                amountWithdrawn(String.valueOf(withdrawalAmountRequested)).build();
    }

    private List<Currency> getAvailableCurrency() {
        Optional<ATM> optionalATM = atmRepository.findById(1L);
        ATM atm = null;
        if (optionalATM.isPresent()) {
            atm = optionalATM.get();
        }
        return currencyRepository.findByAtm(atm, Sort.by(Sort.Direction.ASC, "value"));
    }

    private Integer validateCustomersAccountAndBalance(Optional<CustomerAccount> customerAccount, Integer withdrawalAmountRequested) {
        if (customerAccount.isPresent()) {
            int customerBalance = customerAccount.get().getAccountBalance();
            if (customerBalance < withdrawalAmountRequested) {
                throw new RequestedFundsNotAvailableException();
            }
        } else {
            throw new UnexpectedRuntimeException("Unexpected state. Account should exist if already validated on Request.");
        }

        return customerAccount.get().getAccountBalance();
    }

    private List<Long> withdrawMoney(List<Currency> currencyList, Integer withdrawlAmount) {
        int amountRemainingToWithdraw = withdrawlAmount;
        List<Long> idsOfMoneyToWithDraw = new ArrayList<>();

        for (Currency currencyItem : currencyList) {
            if (amountRemainingToWithdraw - currencyItem.getValue() >= 0) {
                amountRemainingToWithdraw -= currencyItem.getValue();
                idsOfMoneyToWithDraw.add(currencyItem.getId());
            }

            if (amountRemainingToWithdraw == 0) {
                return idsOfMoneyToWithDraw;
            }

        }
        return null;
    }
}

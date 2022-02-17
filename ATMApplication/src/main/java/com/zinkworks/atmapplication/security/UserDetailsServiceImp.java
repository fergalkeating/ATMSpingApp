package com.zinkworks.atmapplication.security;

import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import com.zinkworks.atmapplication.service.atm.account.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImp implements UserDetailsService {
    @Autowired
    AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Getting Account Authentication for User " + username);
        CustomerAccount customerAccount = accountService.getAccountBalance(username);

        if (customerAccount == null) {
            throw new UsernameNotFoundException(username);
        }
        return new CustomerUserDetails(customerAccount);
    }
}

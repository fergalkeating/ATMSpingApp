package com.zinkworks.atmapplication.security;

import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomerUserDetails implements UserDetails {

    private CustomerAccount customerAccount;

    public CustomerUserDetails(CustomerAccount customerAccount) {
        this.customerAccount = customerAccount;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("User"));
    }

    @Override
    public String getPassword() {
        return customerAccount.getPin();
    }

    @Override
    public String getUsername() {
        return customerAccount.getAccountId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public CustomerAccount getCustomerAccount() {
        return customerAccount;
    }
}

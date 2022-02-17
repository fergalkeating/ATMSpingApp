package com.zinkworks.atmapplication.repository.customer;

import com.zinkworks.atmapplication.model.atm.Currency;
import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends CrudRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByAccountId(String accountId);

}

package com.zinkworks.atmapplication.repository.atm;

import com.zinkworks.atmapplication.model.atm.ATM;
import com.zinkworks.atmapplication.model.atm.Currency;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    List<Currency> findByAtm(ATM atm, Sort sort);
}

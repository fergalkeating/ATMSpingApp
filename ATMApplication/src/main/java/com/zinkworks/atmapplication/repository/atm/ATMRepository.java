package com.zinkworks.atmapplication.repository.atm;

import com.zinkworks.atmapplication.model.atm.ATM;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATMRepository extends CrudRepository<ATM, Long> {

}

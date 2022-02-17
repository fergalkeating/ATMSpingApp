package com.zinkworks.atmapplication.model.atm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "atm")
public class ATM {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String ATMName;

    private String ATMBalance;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "atm")
    private List<Currency> currency;

}

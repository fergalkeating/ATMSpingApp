package com.zinkworks.atmapplication.service.atm.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WithdrawFromAccountResponseDto {
    String accountId;
    String accountBalance;
    String amountWithdrawn;
}

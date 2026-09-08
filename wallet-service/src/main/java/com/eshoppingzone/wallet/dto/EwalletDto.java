package com.eshoppingzone.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EwalletDto {

    private Integer walletId;
    private Double currentBalance;
}

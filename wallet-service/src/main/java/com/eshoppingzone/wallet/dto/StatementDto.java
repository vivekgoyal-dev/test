package com.eshoppingzone.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementDto {

    private Integer statementId;
    private Integer walletId;
    private String transactionType;
    private Double amount;
    private LocalDateTime dateTime;
    private Integer orderId;
    private String transactionRemarks;
}

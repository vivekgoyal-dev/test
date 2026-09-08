package com.eshoppingzone.wallet.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statementId;

    private String transactionType;
    private Double amount;
    private LocalDateTime dateTime;
    private Integer orderId;
    private String transactionRemarks;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ewallet ewallet;

    public Statement(Ewallet ewallet, String transactionType, Double amount, LocalDateTime dateTime, Integer orderId,
                     String transactionRemarks) {
        this.ewallet = ewallet;
        this.transactionType = transactionType;
        this.amount = amount;
        this.dateTime = dateTime;
        this.orderId = orderId;
        this.transactionRemarks = transactionRemarks;
    }
}

package com.eshoppingzone.wallet.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Ewallet {

    /** Not generated: the wallet id IS the customer's profile id. */
    @Id
    private Integer walletId;

    private Double currentBalance;

    @OneToMany(mappedBy = "ewallet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Statement> statements = new ArrayList<>();

    public Ewallet(Integer walletId, Double currentBalance) {
        this.walletId = walletId;
        this.currentBalance = currentBalance;
    }
}

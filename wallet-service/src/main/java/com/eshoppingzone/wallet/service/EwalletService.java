package com.eshoppingzone.wallet.service;

import com.eshoppingzone.wallet.dto.EwalletDto;
import com.eshoppingzone.wallet.dto.StatementDto;

import java.util.List;

public interface EwalletService {

    List<EwalletDto> getWallets();

    EwalletDto addWallet(Integer walletId);

    EwalletDto addmoney(int walletId, double amount, String remarks);

    EwalletDto pay(int walletId, double amount, int orderId, String remarks);

    EwalletDto getById(int walletId);

    List<StatementDto> getStatementsById(int walletId);

    List<StatementDto> getStatements();

    void deleteById(int walletId);
}

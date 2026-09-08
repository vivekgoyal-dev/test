package com.eshoppingzone.wallet.repository;

import com.eshoppingzone.wallet.pojo.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatementsRepository extends JpaRepository<Statement, Integer> {

    List<Statement> findByEwalletWalletId(Integer walletId);
}

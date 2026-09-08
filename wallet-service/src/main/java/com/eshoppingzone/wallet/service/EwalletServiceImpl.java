package com.eshoppingzone.wallet.service;

import com.eshoppingzone.wallet.dto.EwalletDto;
import com.eshoppingzone.wallet.dto.StatementDto;
import com.eshoppingzone.wallet.pojo.Ewallet;
import com.eshoppingzone.wallet.pojo.Statement;
import com.eshoppingzone.wallet.repository.EwalletRepository;
import com.eshoppingzone.wallet.repository.StatementsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EwalletServiceImpl implements EwalletService {

    public static final String DEPOSIT = "Deposit";
    public static final String WITHDRAW = "Withdraw";

    private final EwalletRepository repo;
    private final StatementsRepository statementrepo;

    public EwalletServiceImpl(EwalletRepository repo, StatementsRepository statementrepo) {
        this.repo = repo;
        this.statementrepo = statementrepo;
    }

    @Override
    public List<EwalletDto> getWallets() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public EwalletDto addWallet(Integer walletId) {
        if (repo.existsById(walletId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "wallet " + walletId + " already exists");
        }
        return toDto(repo.save(new Ewallet(walletId, 0.0)));
    }

    @Override
    public EwalletDto getById(int walletId) {
        return toDto(findOrThrow(walletId));
    }

    @Override
    @Transactional
    public EwalletDto addmoney(int walletId, double amount, String remarks) {
        Ewallet wallet = findOrThrow(walletId);
        requirePositive(amount);
        wallet.setCurrentBalance(wallet.getCurrentBalance() + amount);
        record(wallet, DEPOSIT, amount, null, remarks);
        return toDto(repo.save(wallet));
    }

    @Override
    @Transactional
    public EwalletDto pay(int walletId, double amount, int orderId, String remarks) {
        Ewallet wallet = findOrThrow(walletId);
        requirePositive(amount);
        if (wallet.getCurrentBalance() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "insufficient balance: has " + wallet.getCurrentBalance() + ", needs " + amount);
        }
        wallet.setCurrentBalance(wallet.getCurrentBalance() - amount);
        record(wallet, WITHDRAW, amount, orderId, remarks);
        return toDto(repo.save(wallet));
    }

    /** The balance never moves without a statement row recording why. */
    private void record(Ewallet wallet, String type, double amount, Integer orderId, String remarks) {
        statementrepo.save(new Statement(wallet, type, amount, LocalDateTime.now(), orderId, remarks));
    }

    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }
    }

    @Override
    public List<StatementDto> getStatementsById(int walletId) {
        findOrThrow(walletId);
        return statementrepo.findByEwalletWalletId(walletId).stream().map(this::toDto).toList();
    }

    @Override
    public List<StatementDto> getStatements() {
        return statementrepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void deleteById(int walletId) {
        repo.delete(findOrThrow(walletId));
    }

    private Ewallet findOrThrow(int walletId) {
        return repo.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no wallet " + walletId));
    }

    private EwalletDto toDto(Ewallet wallet) {
        return new EwalletDto(wallet.getWalletId(), wallet.getCurrentBalance());
    }

    private StatementDto toDto(Statement s) {
        return new StatementDto(s.getStatementId(), s.getEwallet().getWalletId(), s.getTransactionType(), s.getAmount(),
                s.getDateTime(), s.getOrderId(), s.getTransactionRemarks());
    }
}

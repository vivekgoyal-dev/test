package com.eshoppingzone.wallet.resource;

import com.eshoppingzone.wallet.dto.EwalletDto;
import com.eshoppingzone.wallet.dto.StatementDto;
import com.eshoppingzone.wallet.service.EwalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletResource {

    private final EwalletService service;

    public WalletResource(EwalletService service) {
        this.service = service;
    }

    /** Opens a wallet; the wallet id is the customer's profile id. */
    @PostMapping("/{walletId}")
    public ResponseEntity<EwalletDto> addNewWallet(@PathVariable Integer walletId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addWallet(walletId));
    }

    @GetMapping
    public ResponseEntity<List<EwalletDto>> getAllWallet() {
        return ResponseEntity.ok(service.getWallets());
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Double> getById(@PathVariable int walletId) {
        return ResponseEntity.ok(service.getById(walletId).getCurrentBalance());
    }

    @PutMapping("/{walletId}/add")
    public ResponseEntity<EwalletDto> addMoney(@PathVariable int walletId, @RequestParam double amount,
                                               @RequestParam(defaultValue = "Wallet top up") String remarks) {
        return ResponseEntity.ok(service.addmoney(walletId, amount, remarks));
    }

    @PutMapping("/{walletId}/pay")
    public ResponseEntity<EwalletDto> payMoney(@PathVariable int walletId, @RequestParam double amount,
                                               @RequestParam int orderId,
                                               @RequestParam(defaultValue = "Order payment") String remarks) {
        return ResponseEntity.ok(service.pay(walletId, amount, orderId, remarks));
    }

    @GetMapping("/{walletId}/statements")
    public ResponseEntity<List<StatementDto>> getStatementsById(@PathVariable int walletId) {
        return ResponseEntity.ok(service.getStatementsById(walletId));
    }

    @GetMapping("/statements")
    public ResponseEntity<List<StatementDto>> getStatements() {
        return ResponseEntity.ok(service.getStatements());
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteById(@PathVariable int walletId) {
        service.deleteById(walletId);
        return ResponseEntity.noContent().build();
    }
}

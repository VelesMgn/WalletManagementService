package org.example.walletmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/operations")
    public ResponseEntity<Void> processOperation(@RequestBody @Valid WalletOperationRequest request) {
        log.info("WalletController calls walletService");
        walletService.processOperation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID walletId) {
        log.info("WalletController calls getBalance with id {}", walletId);
        WalletBalanceResponse response = walletService.getBalance(walletId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<WalletBalanceResponse> createWallet() {
        log.info("WalletController calls createWallet");
        WalletBalanceResponse response = walletService.createWallet();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WalletBalanceResponse>> getAllWallets() { // можно добавить domain.Page;
        log.info("WalletController calls getAllWallets");
        return ResponseEntity.ok(walletService.getAllWallets());
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID walletId) {
        log.info("WalletController deleting wallet with id: {}", walletId);
        walletService.deleteWallet(walletId);
        return ResponseEntity.noContent().build();
    }
}
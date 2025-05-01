package org.example.walletmanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Void> processOperation(@RequestBody @Valid WalletOperationRequest request) {
        log.info("WalletController calls walletService");
        walletService.processOperation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID walletId) {
        log.info("WalletController calls getBalance");
        WalletBalanceResponse response = walletService.getBalance(walletId);
        return ResponseEntity.ok(response);
    }
}
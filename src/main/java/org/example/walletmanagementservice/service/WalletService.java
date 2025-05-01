package org.example.walletmanagementservice.service;

import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    void processOperation(WalletOperationRequest request);
    WalletBalanceResponse getBalance(UUID walletId);
    WalletBalanceResponse createWallet();
    List<WalletBalanceResponse> getAllWallets();
    void deleteWallet(UUID walletId);
}
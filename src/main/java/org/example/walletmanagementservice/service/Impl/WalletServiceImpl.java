package org.example.walletmanagementservice.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.exception.InsufficientFundsException;
import org.example.walletmanagementservice.exception.WalletNotFoundException;
import org.example.walletmanagementservice.model.Wallet;
import org.example.walletmanagementservice.service.WalletService;
import org.example.walletmanagementservice.service.database.WalletDatabaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletDatabaseService walletDatabase;

    @Override
    public void processOperation(WalletOperationRequest request) {
        Wallet wallet = walletDatabase.getWallet(request.getWalletId());
        if (wallet == null) throw new WalletNotFoundException(request.getWalletId());

        BigDecimal newBalance = changeBalance(request, wallet);
        wallet.setBalance(newBalance);

        walletDatabase.updateWallet(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletDatabase.getWallet(walletId);
        if (wallet == null) throw new WalletNotFoundException(walletId);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .build();
    }

    @Override
    @Transactional
    public WalletBalanceResponse createWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setBalance(BigDecimal.ZERO);

        walletDatabase.updateWallet(wallet);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .build();
    }

    @Override
    public List<WalletBalanceResponse> getAllWallets() {
        return convertToDto(walletDatabase.getAllWallet());
    }

    @Override
    public void deleteWallet(UUID walletId) {
        if(walletDatabase.getWallet(walletId) == null) throw new WalletNotFoundException(walletId);
        walletDatabase.deleteWallet(walletId);
    }

    private List<WalletBalanceResponse> convertToDto(List<Wallet> wallets) {
        List<WalletBalanceResponse> walletBalanceResponses = new ArrayList<>();
        wallets.forEach(wallet -> {
            walletBalanceResponses.add(new WalletBalanceResponse(wallet.getId(), wallet.getBalance()));
        });
        return walletBalanceResponses;
    }

    private BigDecimal changeBalance(WalletOperationRequest request, Wallet wallet) {
        BigDecimal newBalance = BigDecimal.ZERO;

        switch (request.getOperationType()){
            case DEPOSIT -> newBalance = wallet.getBalance().add(request.getAmount());
            case WITHDRAW -> {
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientFundsException(request.getWalletId());
                }
                newBalance = wallet.getBalance().subtract(request.getAmount());
            }
        }

        return newBalance;
    }
}
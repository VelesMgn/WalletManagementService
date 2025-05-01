package org.example.walletmanagementservice.service.Impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.exception.InsufficientFundsException;
import org.example.walletmanagementservice.exception.WalletNotFoundException;
import org.example.walletmanagementservice.model.Wallet;
import org.example.walletmanagementservice.service.WalletService;
import org.example.walletmanagementservice.service.database.WalletDatabaseService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private static final int MAX_RETRIES = 10;

    @Override
    public void processOperation(WalletOperationRequest request) {
        int retries = 0;

        while (true) {
            try {
                Wallet wallet = walletDatabase.getWallet(request.getWalletId())
                        .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

                BigDecimal newBalance = changeBalance(request, wallet);
                wallet.setBalance(newBalance);

                walletDatabase.updateWallet(wallet);
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (++retries >= MAX_RETRIES) throw e;
                log.warn("Optimistic lock failed, retrying ({})", retries);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletDatabase.getWallet(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

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
        return convertToDto(walletDatabase.getAllWallets());
    }

    @Override
    public void deleteWallet(UUID walletId) {
        if(walletDatabase.getWallet(walletId).isEmpty()) throw new WalletNotFoundException(walletId);
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
        return switch (request.getOperationType()) {
            case DEPOSIT -> wallet.getBalance().add(request.getAmount());
            case WITHDRAW -> {
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientFundsException(request.getWalletId());
                }
                yield wallet.getBalance().subtract(request.getAmount());
            }
        };
    }
}
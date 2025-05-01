package org.example.walletmanagementservice.service.database;

import lombok.RequiredArgsConstructor;
import org.example.walletmanagementservice.model.Wallet;
import org.example.walletmanagementservice.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletDatabaseService {
    private final WalletRepository walletRepository;

    public Optional<Wallet> getWallet(UUID walletId) {
        return null;
    }
}

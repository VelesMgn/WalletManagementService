package org.example.walletmanagementservice.service.database;

import lombok.RequiredArgsConstructor;
import org.example.walletmanagementservice.model.Wallet;
import org.example.walletmanagementservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletDatabaseService {
    private final WalletRepository walletRepository;

    public Wallet getWallet(UUID walletId) {
        return walletRepository.findWalletById(walletId);
    }

    public void updateWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallet() {
        return walletRepository.findAll();
    }

    @Transactional
    public void deleteWallet(UUID walletId) {
        walletRepository.deleteById(walletId);
    }
}

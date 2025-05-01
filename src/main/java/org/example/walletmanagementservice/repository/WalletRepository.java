package org.example.walletmanagementservice.repository;

import org.example.walletmanagementservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findWalletById(UUID id);
    void deleteById(UUID id);
}

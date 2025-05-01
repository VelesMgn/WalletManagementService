package org.example.walletmanagementservice.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.service.WalletService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    @Override
    public void processOperation(WalletOperationRequest request) {

    }

    @Override
    public WalletBalanceResponse getBalance(UUID walletId) {
        return null;
    }
}

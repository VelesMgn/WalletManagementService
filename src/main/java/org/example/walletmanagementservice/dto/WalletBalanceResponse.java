package org.example.walletmanagementservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WalletBalanceResponse {
    private UUID walletId;
    private BigDecimal balance;
}
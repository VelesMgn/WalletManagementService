package org.example.walletmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class WalletBalanceResponse {
    private UUID walletId;
    private BigDecimal balance;
}
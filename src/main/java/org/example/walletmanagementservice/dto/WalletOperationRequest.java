package org.example.walletmanagementservice.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.example.walletmanagementservice.model.enums.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WalletOperationRequest {
    @NotNull
    private UUID walletId;

    @NotNull
    private OperationType operationType;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", message = "Balance must be positive")
    private BigDecimal amount;
}

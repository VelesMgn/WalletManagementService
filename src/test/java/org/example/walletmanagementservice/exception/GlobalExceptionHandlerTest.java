package org.example.walletmanagementservice.exception;

import org.example.walletmanagementservice.controller.WalletController;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.model.enums.OperationType;
import org.example.walletmanagementservice.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private MockMvc mockMvc;


    @Test
    void handleWalletNotFound_ShouldReturnNotFoundResponse() throws Exception {
        WalletService walletService = mock(WalletService.class);
        UUID walletId = UUID.randomUUID();

        when(walletService.getBalance(walletId))
                .thenThrow(new WalletNotFoundException(walletId));

        WalletController controller = new WalletController(walletService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Wallet not found with id: "
                        + walletId));
    }

    @Test
    void handleInsufficientFunds_ShouldReturnBadRequest() throws Exception {
        WalletController controller = mock(WalletController.class);
        when(controller.processOperation(any()))
                .thenThrow(new InsufficientFundsException(UUID.randomUUID()));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(UUID.randomUUID());
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/wallets/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + request.getWalletId()
                                + "\",\"operationType\":\"WITHDRAW\",\"amount\":100.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(null))
                .setControllerAdvice(exceptionHandler)
                .build();

        mockMvc.perform(post("/api/v1/wallets/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"550e8400-e29b-41d4-a716-446655440000\"" +
                                ",\"operationType\":\"DEPOSIT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Validation error: Amount cannot be null"));

        mockMvc.perform(post("/api/v1/wallets/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"550e8400-e29b-41d4-a716-446655440000\"" +
                                ",\"operationType\":\"DEPOSIT\",\"amount\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Validation error: Balance must be positive"));
    }

    @Test
    void handleInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(null))
                .setControllerAdvice(exceptionHandler)
                .build();

        mockMvc.perform(post("/api/v1/wallets/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("INVALID_JSON"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Malformed or invalid JSON in request body"));
    }

    @Test
    void handleUnexpected_ShouldReturnInternalServerError() throws Exception {
        WalletController controller = mock(WalletController.class);
        when(controller.getBalance(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();

        mockMvc.perform(get("/api/v1/wallets/{walletId}", UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode")
                        .value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Unexpected error: Unexpected error"));
    }
}

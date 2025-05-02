package org.example.walletmanagementservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.exception.GlobalExceptionHandler;
import org.example.walletmanagementservice.model.enums.OperationType;
import org.example.walletmanagementservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void processOperation_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(UUID.randomUUID());
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/wallets/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService).processOperation(any(WalletOperationRequest.class));
    }

    @Test
    void getBalance_ShouldReturnWalletBalance_WhenWalletExists() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .walletId(walletId)
                .balance(new BigDecimal("500.00"))
                .build();

        when(walletService.getBalance(walletId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(walletService).getBalance(walletId);
    }

    @Test
    void createWallet_ShouldReturnCreated_WithNewWallet() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .walletId(walletId)
                .balance(BigDecimal.ZERO)
                .build();

        when(walletService.createWallet()).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(0));

        verify(walletService).createWallet();
    }

    @Test
    void getAllWallets_ShouldReturnListOfWallets() throws Exception {
        UUID walletId1 = UUID.randomUUID();
        UUID walletId2 = UUID.randomUUID();
        List<WalletBalanceResponse> responses = List.of(
                WalletBalanceResponse.builder().walletId(walletId1).balance(new BigDecimal("100.00")).build(),
                WalletBalanceResponse.builder().walletId(walletId2).balance(new BigDecimal("200.00")).build()
        );

        when(walletService.getAllWallets()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].walletId").value(walletId1.toString()))
                .andExpect(jsonPath("$[0].balance").value(100.00))
                .andExpect(jsonPath("$[1].walletId").value(walletId2.toString()))
                .andExpect(jsonPath("$[1].balance").value(200.00));

        verify(walletService).getAllWallets();
    }

    @Test
    void deleteWallet_ShouldReturnNoContent_WhenWalletExists() throws Exception {
        UUID walletId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isNoContent());

        verify(walletService).deleteWallet(walletId);
    }
}
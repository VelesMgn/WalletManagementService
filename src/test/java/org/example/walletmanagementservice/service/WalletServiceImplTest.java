package org.example.walletmanagementservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.OptimisticLockException;
import org.example.walletmanagementservice.dto.WalletBalanceResponse;
import org.example.walletmanagementservice.dto.WalletOperationRequest;
import org.example.walletmanagementservice.exception.InsufficientFundsException;
import org.example.walletmanagementservice.exception.WalletNotFoundException;
import org.example.walletmanagementservice.model.Wallet;
import org.example.walletmanagementservice.model.enums.OperationType;
import org.example.walletmanagementservice.service.Impl.WalletServiceImpl;
import org.example.walletmanagementservice.service.database.WalletDatabaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletDatabaseService walletDatabase;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void processOperation_ShouldUpdateBalance_ForDeposit() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("50.00"));

        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));
        doNothing().when(walletDatabase).updateWallet(any(Wallet.class));

        walletService.processOperation(request);

        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
        verify(walletDatabase).updateWallet(wallet);
    }

    @Test
    void processOperation_ShouldUpdateBalance_ForWithdraw() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("50.00"));

        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));
        doNothing().when(walletDatabase).updateWallet(any(Wallet.class));

        walletService.processOperation(request);

        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
        verify(walletDatabase).updateWallet(wallet);
    }

    @Test
    void processOperation_ShouldThrowInsufficientFunds_WhenBalanceIsNotEnough() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("150.00"));

        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () -> walletService.processOperation(request));
        verify(walletDatabase, never()).updateWallet(any());
    }

    @Test
    void processOperation_ShouldThrowWalletNotFound_WhenWalletDoesNotExist() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("50.00"));

        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.processOperation(request));
        verify(walletDatabase, never()).updateWallet(any());
    }

    @Test
    void processOperation_ShouldRetry_WhenOptimisticLockExceptionOccurs() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("50.00"));

        Wallet firstWallet = new Wallet();
        firstWallet.setId(walletId);
        firstWallet.setBalance(new BigDecimal("100.00"));

        Wallet secondWallet = new Wallet();
        secondWallet.setId(walletId);
        secondWallet.setBalance(new BigDecimal("100.00"));

        when(walletDatabase.getWallet(walletId))
                .thenReturn(Optional.of(firstWallet))
                .thenReturn(Optional.of(secondWallet));

        doThrow(new ObjectOptimisticLockingFailureException("", new Exception()))
                .doNothing()
                .when(walletDatabase).updateWallet(any(Wallet.class));

        walletService.processOperation(request);

        verify(walletDatabase, times(2)).updateWallet(any(Wallet.class));
    }

    @Test
    void processOperation_ShouldThrow_WhenMaxRetriesExceeded() {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("50.00"));

        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));
        doThrow(new OptimisticLockException())
                .when(walletDatabase).updateWallet(any(Wallet.class));

        assertThrows(OptimisticLockException.class, () -> walletService.processOperation(request));
        verify(walletDatabase, times(10)).updateWallet(any(Wallet.class));
    }

    @Test
    void getBalance_ShouldReturnBalanceResponse_WhenWalletExists() {
        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));

        WalletBalanceResponse response = walletService.getBalance(walletId);

        assertEquals(walletId, response.getWalletId());
        assertEquals(new BigDecimal("100.00"), response.getBalance());
    }

    @Test
    void getBalance_ShouldThrowWalletNotFound_WhenWalletDoesNotExist() {
        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getBalance(walletId));
    }

    @Test
    void createWallet_ShouldCreateNewWalletWithZeroBalance() {
        doNothing().when(walletDatabase).updateWallet(any(Wallet.class));

        WalletBalanceResponse response = walletService.createWallet();

        assertNotNull(response.getWalletId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletDatabase).updateWallet(any(Wallet.class));
    }

    @Test
    void getAllWallets_ShouldReturnListOfWalletResponses() {
        Wallet wallet1 = new Wallet();
        wallet1.setId(UUID.randomUUID());
        wallet1.setBalance(new BigDecimal("100.00"));

        Wallet wallet2 = new Wallet();
        wallet2.setId(UUID.randomUUID());
        wallet2.setBalance(new BigDecimal("200.00"));

        when(walletDatabase.getAllWallets()).thenReturn(List.of(wallet1, wallet2));

        List<WalletBalanceResponse> responses = walletService.getAllWallets();

        assertEquals(2, responses.size());
        assertEquals(wallet1.getId(), responses.get(0).getWalletId());
        assertEquals(wallet1.getBalance(), responses.get(0).getBalance());
        assertEquals(wallet2.getId(), responses.get(1).getWalletId());
        assertEquals(wallet2.getBalance(), responses.get(1).getBalance());
    }

    @Test
    void deleteWallet_ShouldDeleteWallet_WhenItExists() {
        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.of(wallet));

        walletService.deleteWallet(walletId);

        verify(walletDatabase).deleteWallet(walletId);
    }

    @Test
    void deleteWallet_ShouldThrowWalletNotFound_WhenWalletDoesNotExist() {
        when(walletDatabase.getWallet(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.deleteWallet(walletId));
        verify(walletDatabase, never()).deleteWallet(walletId);
    }
}
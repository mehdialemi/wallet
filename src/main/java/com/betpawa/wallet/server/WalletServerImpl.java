package com.betpawa.wallet.server;

import com.betpawa.wallet.commons.*;
import com.betpawa.wallet.repository.Account;
import com.betpawa.wallet.repository.Balance;
import com.betpawa.wallet.services.AccountService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletServerImpl extends WalletTransactionGrpc.WalletTransactionImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WalletServerImpl.class);

    public static final Empty EMPTY = Empty.newBuilder().build();

    private AccountService accountService;

    public WalletServerImpl() {
        accountService = new AccountService();
    }

    @Override
    public void deposit(DepositRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Received deposit request {}", request);

        try {
            accountService.deposite(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(t);
        }
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver <Empty> responseObserver) {
        logger.info("Received withdraw request {}", request);

        try {
            accountService.withdraw(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(t);
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver <BalanceResponse> responseObserver) {
        logger.info("Received balance request {}", request);
        try {
            Account account = accountService.balance(request);
            BalanceResponse.Builder balanceResponseBuilder = BalanceResponse.newBuilder();
            for (Balance balance: account.getBalance()) {
                balanceResponseBuilder
                        .addResults(BalanceResult.newBuilder()
                        .setAmount(balance.getAmount())
                        .setCurrency(balance.getCurrency()));
            }
            responseObserver.onNext(balanceResponseBuilder.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(t);
        }
    }
}

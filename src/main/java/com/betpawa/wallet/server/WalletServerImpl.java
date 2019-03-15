package com.betpawa.wallet.server;

import com.betpawa.wallet.commons.*;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import com.betpawa.wallet.repository.Account;
import com.betpawa.wallet.repository.Balance;
import com.betpawa.wallet.services.AccountService;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WalletServerImpl extends WalletTransactionGrpc.WalletTransactionImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WalletServerImpl.class);

    private static final Empty EMPTY = Empty.newBuilder().build();
    private final MetricRegistry metrics = new MetricRegistry();
    private Meter depositRequests;
    private Meter withdrawRequests;
    private Meter balanceRequests;

    private AccountService accountService;

    WalletServerImpl() {
        accountService = new AccountService();
        depositRequests = metrics.meter("deposit-requests");
        withdrawRequests = metrics.meter("withdraw-requests");
        balanceRequests = metrics.meter("balance-requests");
        // load session factory at initialization
        HibernateUtil.getSessionFactory();
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);
    }

    @Override
    public void deposit(DepositRequest request, StreamObserver<Empty> responseObserver) {
        logger.trace("Received deposit request {}", StringUtil.toString(request));
        depositRequests.mark();
        try {
            accountService.deposit(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (UnknownCurrencyException e) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver <Empty> responseObserver) {
        logger.trace("Received withdraw request {}", StringUtil.toString(request));
        withdrawRequests.mark();
        try {
            accountService.withdraw(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (InSufficientFundException e) {
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver <BalanceResponse> responseObserver) {
        logger.trace("Received balance request {}", StringUtil.toString(request));
        balanceRequests.mark();
        try {
            Account account = accountService.getAccount(request.getUserId());
            Set<Balance> balances = account == null ? new HashSet <>() : account.getBalance();

            BalanceResponse.Builder balanceResponseBuilder = BalanceResponse.newBuilder();
            for (Balance balance: balances) {
                balanceResponseBuilder
                        .addResults(BalanceResult.newBuilder()
                        .setAmount(balance.getAmount())
                        .setCurrency(balance.getCurrency()));
            }

            responseObserver.onNext(balanceResponseBuilder.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription(t.getMessage())));
        }
    }
}

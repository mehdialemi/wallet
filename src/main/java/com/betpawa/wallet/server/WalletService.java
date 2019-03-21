package com.betpawa.wallet.server;

import com.betpawa.wallet.account.Account;
import com.betpawa.wallet.account.AccountService;
import com.betpawa.wallet.commons.*;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WalletService extends WalletTransactionGrpc.WalletTransactionImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private static final Empty EMPTY = Empty.newBuilder().build();
    private final Timer timer;
    private AccountService accountService;

    WalletService(WalletConfig config) {
        accountService = new AccountService();
        MetricRegistry metrics = new MetricRegistry();
        timer = metrics.timer("request.delay");
        // load session factory at initialization
        HibernateUtil.getSessionFactory();
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger("wallet.server"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.SECONDS)
                .build();
        reporter.start(config.getReportPeriodSec(), TimeUnit.SECONDS);
    }

    @Override
    public void registerUser(UserId request, StreamObserver <Empty> responseObserver) {
        logger.info("Create new account for userId: {}", request.getUserId());
        Timer.Context context = timer.time();
        try {
            accountService.register(request.getUserId());
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (UnknownCurrencyException e) {
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
        } finally {
            context.stop();
        }
    }

    @Override
    public void deposit(DepositRequest request, StreamObserver<Empty> responseObserver) {
        logger.trace("Received deposit request {}", StringUtil.toString(request));
        Timer.Context context = timer.time();
        try {
            accountService.deposit(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (UnknownCurrencyException e) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription(e.getMessage())));
        } finally {
            context.stop();
        }
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver <Empty> responseObserver) {
        logger.trace("Received withdraw request {}", StringUtil.toString(request));
        Timer.Context context = timer.time();
        try {
            accountService.withdraw(request);
            responseObserver.onNext(EMPTY);
            responseObserver.onCompleted();
        } catch (InSufficientFundException e) {
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
        } finally {
            context.stop();
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver <BalanceResponse> responseObserver) {
        logger.trace("Received balance request {}", StringUtil.toString(request));
        Timer.Context context = timer.time();
        try {
            List <Account> accounts = accountService.getAccount(request.getUserId());

            BalanceResponse.Builder balanceResponseBuilder = BalanceResponse.newBuilder();
            for (Account account: accounts) {
                balanceResponseBuilder
                        .addResults(BalanceResult.newBuilder()
                        .setAmount(account.getAmount())
                        .setCurrency(account.getAccountPK().getCurrency()));
            }

            responseObserver.onNext(balanceResponseBuilder.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription(t.getMessage())));
        } finally {
            context.stop();
        }
    }
}

package com.betpawa.wallet.server;

import com.betpawa.wallet.account.Account;
import com.betpawa.wallet.account.AccountService;
import com.betpawa.wallet.commons.HibernateUtil;
import com.betpawa.wallet.commons.StringUtil;
import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import com.betpawa.wallet.proto.*;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.protobuf.Empty;
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

    public final static Wallet.Response SUCCESS = Wallet.Response
            .newBuilder()
            .setSuccess(true)
            .build();

    public final static Wallet.Response INSUFFICIENT_FOUND = Wallet.Response
            .newBuilder()
            .setSuccess(false)
            .setMessage(InSufficientFundException.MESSAGE)
            .build();

    public final static Wallet.Response UNKNOWN_CURRENCY = Wallet.Response
            .newBuilder()
            .setSuccess(false)
            .setMessage(UnknownCurrencyException.MESSAGE)
            .build();

    WalletService(WalletConfig config) {
        HibernateUtil.getSessionFactory();
        accountService = new AccountService();

        MetricRegistry metrics = new MetricRegistry();
        timer = metrics.timer("request.delay");
        final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.SECONDS)
                .build();
        reporter.start(config.getReportPeriodSec(), TimeUnit.SECONDS);
    }

    @Override
    public void call(Wallet.Request request, StreamObserver <Wallet.Response> responseObserver) {
        perform(request, responseObserver);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver <Wallet.Request> streamCall(StreamObserver <Wallet.Response> responseObserver) {
        return new StreamObserver<Wallet.Request>() {

            @Override
            public void onNext(Wallet.Request request) {
                perform(request, responseObserver);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("stream call cancelled");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void registerUser(Wallet.UserId request, StreamObserver <Empty> responseObserver) {
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

    private void perform(Wallet.Request request, StreamObserver <Wallet.Response> responseObserver) {
        Timer.Context context = timer.time();
        try {
            switch (request.getOp()) {
                case BALANCE:
                    logger.debug("Received balance request, userId: {}", request.getUserId());
                    List<Account> account = accountService.getAccount(request.getUserId());
                    responseObserver.onNext(balanceResponse(account));
                    break;

                case WITHDRAW:
                    try {
                        logger.debug("Received withdraw request as {}", StringUtil.toString(request));
                        accountService.withdraw(request.getUserId(), request.getAmount(), request.getCurrency());
                        responseObserver.onNext(SUCCESS);
                    } catch (InSufficientFundException e) {
                        responseObserver.onNext(INSUFFICIENT_FOUND);
                    } catch (UnknownCurrencyException e) {
                        responseObserver.onNext(UNKNOWN_CURRENCY);
                    }

                    break;

                case DEPOSIT:
                    try {
                        logger.debug("Received deposit request as {}", StringUtil.toString(request));
                        accountService.deposit(request.getUserId(), request.getAmount(), request.getCurrency());
                        responseObserver.onNext(SUCCESS);
                    }  catch (InSufficientFundException e) {
                        responseObserver.onNext(INSUFFICIENT_FOUND);
                    } catch (UnknownCurrencyException e) {
                        responseObserver.onNext(UNKNOWN_CURRENCY);
                    }
                    break;

                case UNRECOGNIZED:
                    responseObserver.onNext(UNKNOWN_CURRENCY);
            }
        } catch (Throwable t) {
            logger.warn("Server throws exception {}", t.getMessage());
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(t.getMessage())));
        } finally {
            context.stop();
        }
    }

    private Wallet.Response balanceResponse(List<Account> accounts) {
        Wallet.Response.Builder builder = Wallet.Response.newBuilder();
        builder.setSuccess(true);
        for (Account account: accounts) {
            builder
                    .addResults(Wallet.BalanceResult.newBuilder()
                            .setAmount(account.getAmount())
                            .setCurrency(account.getAccountPK().getCurrency()));
        }
        return builder.build();
    }
}

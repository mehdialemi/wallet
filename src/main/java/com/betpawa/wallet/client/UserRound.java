package com.betpawa.wallet.client;

import com.betpawa.wallet.proto.ClientRequest;
import com.betpawa.wallet.proto.Currency;
import com.betpawa.wallet.proto.Operation;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserRound {
    private static final Logger logger = LoggerFactory.getLogger(UserRound.class);
    private static Random random = new Random();

    private static Timer operationTimer;
    private static List<StreamObserver <ClientRequest>> streamObserverList = new ArrayList <>();
    public static void setOperationTimer(Timer timer) {
        operationTimer = timer;
    }

    public static void randomRound(WalletClient client, String userId) {
        if (operationTimer == null) {
            MetricRegistry metricRegistry = new MetricRegistry();
            operationTimer = metricRegistry.timer("user.round.operationTimer");
        }
        int random = UserRound.random.nextInt(3);
        switch (random) {
            case 0: roundA(client, userId);
            break;
            case 1: roundB(client, userId);
            break;
            case 2: roundC(client, userId);
            break;
        }
        return;
    }

    private static ClientRequest createDepositRequest(String userId, double amount, Currency currency) {
        return ClientRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .setOp(Operation.DEPOSIT)
                .build();
    }

    private static ClientRequest createWithdrawRequest(String userId, double amount, Currency currency) {
        return ClientRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .setOp(Operation.WITHDRAW)
                .build();
    }

    private static ClientRequest createBalanceRequest(String userId) {
        return ClientRequest.newBuilder()
                .setUserId(userId)
                .setOp(Operation.BALANCE)
                .build();
    }

    public static void roundA(WalletClient client, String userId) {
        logger.debug("Executing round A for userId {}", userId);

//        List<ClientRequest> requests = new ArrayList <>();
        Timer.Context time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
//        requests.add(createDepositRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 200.0, Currency.USD);
//        requests.add(createWithdrawRequest(userId, 200, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.deposit(userId, 100, Currency.EUR);
//        requests.add(createDepositRequest(userId, 100, Currency.EUR));
        time.stop();
//
        time = operationTimer.time();
        client.balance(userId);
//        requests.add(createBalanceRequest(userId));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
//        requests.add(createWithdrawRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.balance(userId);
//        requests.add(createBalanceRequest(userId));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
//        requests.add(createWithdrawRequest(userId, 100, Currency.USD));
        time.stop();
//
//        client.setResultProvider(new WalletClient.ResultProvider() {
//            @Override
//            public void onResponse(BalanceResponse message) {
//                logger.info("received response for round A");
//            }
//
//            @Override
//            public void onRpcError(Throwable exception) {
//                logger.warn(exception.getMessage());
//            }
//        });
//
//        Timer.Context time = operationTimer.time();
//        client.sendAllRequests(requests);
//        time.stop();
    }

    public static void roundB(WalletClient client, String userId) {
        logger.debug("Executing round B for userId {}", userId);
//        List<ClientRequest> requests = new ArrayList <>();
        Timer.Context time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
//        requests.add(createWithdrawRequest(userId, 100, Currency.GBP));
        time.stop();
//
        time = operationTimer.time();
        client.deposit(userId, 300.0, Currency.GBP);
//        requests.add(createDepositRequest(userId, 300, Currency.GBP));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
//        requests.add(createWithdrawRequest(userId, 100, Currency.GBP));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
//        requests.add(createWithdrawRequest(userId, 100, Currency.GBP));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
//        requests.add(createWithdrawRequest(userId, 100, Currency.GBP));
        time.stop();
//
//        client.setResultProvider(new WalletClient.ResultProvider() {
//            @Override
//            public void onResponse(BalanceResponse response) {
//                logger.info("received response for round B");
//            }
//
//            @Override
//            public void onRpcError(Throwable exception) {
//                logger.warn(exception.getMessage());
//            }
//        });
//
//        Timer.Context time = operationTimer.time();
//        client.sendAllRequests(requests);
//        time.stop();
    }

    public static void roundC(WalletClient client, String userId) {
        logger.debug("Executing round C for userId {}", userId);
//        List<ClientRequest> requests = new ArrayList <>();

        Timer.Context time = operationTimer.time();
        client.balance(userId);
//        requests.add(createBalanceRequest(userId));
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
//        requests.add(createDepositRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
//        requests.add(createDepositRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
//        requests.add(createWithdrawRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
//        requests.add(createDepositRequest(userId, 100, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.balance(userId);
//        requests.add(createBalanceRequest(userId));
        time.stop();
//
        time = operationTimer.time();
        client.withdraw(userId, 200.0, Currency.USD);
//        requests.add(createWithdrawRequest(userId, 200, Currency.USD));
        time.stop();
//
        time = operationTimer.time();
        client.balance(userId);
//        requests.add(createBalanceRequest(userId));
        time.stop();
//
//        client.setResultProvider(new WalletClient.ResultProvider() {
//            @Override
//            public void onResponse(BalanceResponse response) {
//                logger.info("received response for round C");
//            }
//
//            @Override
//            public void onRpcError(Throwable exception) {
//                logger.warn(exception.getMessage());
//            }
//        });
//
//        Timer.Context time = operationTimer.time();
//        client.sendAllRequests(requests);
//        time.stop();
    }

}

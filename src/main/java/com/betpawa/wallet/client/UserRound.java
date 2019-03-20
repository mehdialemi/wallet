package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.Currency;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class UserRound {
    private static final Logger logger = LoggerFactory.getLogger(UserRound.class);
    private static Random random = new Random();

    private static Timer operationTimer;
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

    public static void roundA(WalletClient client, String userId) {
        logger.debug("Executing round A for userId {}", userId);

        Timer.Context time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 200.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 100, Currency.EUR);
        time.stop();

        time = operationTimer.time();
        client.balance(userId);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.balance(userId);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
        time.stop();
    }

    public static void roundB(WalletClient client, String userId) {
        logger.debug("Executing round B for userId {}", userId);

        Timer.Context time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 300.0, Currency.GBP);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.GBP);
        time.stop();
    }

    public static void roundC(WalletClient client, String userId) {
        logger.debug("Executing round C for userId {}", userId);

        Timer.Context time = operationTimer.time();
        client.balance(userId);
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.deposit(userId, 100.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.balance(userId);
        time.stop();

        time = operationTimer.time();
        client.withdraw(userId, 200.0, Currency.USD);
        time.stop();

        time = operationTimer.time();
        client.balance(userId);
        time.stop();
    }
}

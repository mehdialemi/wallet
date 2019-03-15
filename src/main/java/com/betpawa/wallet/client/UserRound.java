package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class UserRound {
    private static final Logger logger = LoggerFactory.getLogger(UserRound.class);
    private static Random random = new Random();

    public static void randomRound(WalletClient client, String userId) {
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
        client.deposit(userId, 100.0, Currency.USD);
        client.withdraw(userId, 200.0, Currency.USD);
        client.deposit(userId, 100, Currency.EUR);
        client.balance(userId);
        client.withdraw(userId, 100.0, Currency.USD);
        client.balance(userId);
        client.withdraw(userId, 100.0, Currency.USD);
    }

    public static void roundB(WalletClient client, String userId) {
        logger.debug("Executing round B for userId {}", userId);
        client.withdraw(userId, 100.0, Currency.GBP);
        client.deposit(userId, 300.0, Currency.GBP);
        client.withdraw(userId, 100.0, Currency.GBP);
        client.withdraw(userId, 100.0, Currency.GBP);
        client.withdraw(userId, 100.0, Currency.GBP);
    }

    public static void roundC(WalletClient client, String userId) {
        logger.debug("Executing round C for userId {}", userId);
        client.balance(userId);
        client.deposit(userId, 100.0, Currency.USD);
        client.deposit(userId, 100.0, Currency.USD);
        client.withdraw(userId, 100.0, Currency.USD);
        client.deposit(userId, 100.0, Currency.USD);
        client.balance(userId);
        client.withdraw(userId, 200.0, Currency.USD);
        client.balance(userId);
    }
}

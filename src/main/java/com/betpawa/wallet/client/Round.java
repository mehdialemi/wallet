package com.betpawa.wallet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Round {
    private static final Logger logger = LoggerFactory.getLogger(Round.class);

    public static void roundA(WalletClient client, String userId) {
        logger.info("Executing round A for userId {}", userId);
//        client.deposit(userId, 100.0);
    }

    public static void roundB(WalletClient client, String userId) {
        logger.info("Executing round B for userId {}", userId);
    }

    public static void roundC(WalletClient client, String userId) {
        logger.info("Executing round C for userId {}", userId);
    }

}

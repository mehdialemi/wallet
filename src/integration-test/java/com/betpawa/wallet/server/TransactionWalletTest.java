package com.betpawa.wallet.server;

import com.betpawa.wallet.client.WalletClient;
import com.betpawa.wallet.commons.BalanceResponse;
import com.betpawa.wallet.commons.BalanceResult;
import com.betpawa.wallet.commons.Currency;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static com.betpawa.wallet.commons.Constants.SERVER_PORT;
import static org.junit.Assert.*;

public class TransactionWalletTest {

    private static final String OK = "ok";
    private static WalletServer walletServer;
    private static int port;

    @BeforeClass
    public static void setupClass() throws IOException {
        walletServer = new WalletServer();
        port = new ServerSocket(0).getLocalPort();
        walletServer.start(port);
    }

    @AfterClass
    public static void afterClass() {
        if (walletServer != null) {
            walletServer.stop();
        }
    }

    @Test
    public void sample1() throws InterruptedException {

        WalletClient client = new WalletClient("localhost", port);
        String userId = "1";

        try {
            String msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.USD);
            assertEquals(OK, msg);

            BalanceResponse balance = client.balance(userId);
            assertNotNull(balance);
            assertEquals(1, balance.getResultsCount());
            List <BalanceResult> resultsList = balance.getResultsList();
            assertEquals(100.0, resultsList.get(0).getAmount(), 000.1);
            assertEquals(Currency.USD, resultsList.get(0).getCurrency());

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.EUR);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertNotNull(balance);
            assertEquals(2, balance.getResultsCount());
            BalanceResponse expected = BalanceResponse.newBuilder()
                    .addResults(BalanceResult.newBuilder()
                            .setAmount(100.0)
                            .setCurrency(Currency.EUR)
                    ).addResults(BalanceResult.newBuilder()
                            .setAmount(100.0)
                            .setCurrency(Currency.USD)
                    ).build();
            resultsList = balance.getResultsList();
            assertTrue(expected.getResultsList().contains(resultsList.get(0)));
            assertTrue(expected.getResultsList().contains(resultsList.get(1)));

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.USD);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertEquals(2, balance.getResultsCount());
            expected = BalanceResponse.newBuilder()
                    .addResults(BalanceResult.newBuilder()
                            .setAmount(100.0)
                            .setCurrency(Currency.EUR)
                    ).addResults(BalanceResult.newBuilder()
                            .setAmount(200.0)
                            .setCurrency(Currency.USD)
                    ).build();
            resultsList = balance.getResultsList();
            assertTrue(expected.getResultsList().contains(resultsList.get(0)));
            assertTrue(expected.getResultsList().contains(resultsList.get(1)));


            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertEquals(2, balance.getResultsCount());
            resultsList = balance.getResultsList();
            expected = BalanceResponse.newBuilder()
                    .addResults(BalanceResult.newBuilder()
                            .setAmount(100.0)
                            .setCurrency(Currency.EUR)
                    ).addResults(BalanceResult.newBuilder()
                            .setAmount(0.0)
                            .setCurrency(Currency.USD)
                    ).build();
            assertTrue(expected.getResultsList().contains(resultsList.get(0)));
            assertTrue(expected.getResultsList().contains(resultsList.get(1)));

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

        } finally {
            client.shutdown();
        }
    }
}

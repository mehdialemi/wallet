package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.BalanceResponse;
import com.betpawa.wallet.commons.BalanceResult;
import com.betpawa.wallet.commons.Currency;
import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.server.WalletServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionWalletTest {

    private static final String OK = "ok";
    private static WalletServer walletServer;
    private static int port;

    @BeforeClass
    public static void setupClass() throws IOException {
        WalletConfig config = new WalletConfig();
        config.setPort(new ServerSocket(0).getLocalPort());
        port = config.getPort();
        walletServer = new WalletServer(config);
        walletServer.start();
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
            client.registerUser(userId);

            String msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.USD);
            assertEquals(OK, msg);

            BalanceResponse balance = client.balance(userId);
            assertNotNull(balance);
            assertBalances(createBalances(100, 0, 0), balance.getResultsList());

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.EUR);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertNotNull(balance);
            assertBalances(createBalances(100, 100, 0), balance.getResultsList());

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

            msg = client.deposit(userId, 100.0, Currency.USD);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertNotNull(balance);
            assertBalances(createBalances(200, 100, 0), balance.getResultsList());

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(OK, msg);

            balance = client.balance(userId);
            assertNotNull(balance);
            assertBalances(createBalances(0, 100, 0), balance.getResultsList());

            msg = client.withdraw(userId, 200.0, Currency.USD);
            assertEquals(InSufficientFundException.MESSAGE, msg);

        } finally {
            client.shutdown();
        }
    }

    private List <BalanceResult> createBalances(double usdVal, double eurVal, double gbpVal) {
        return BalanceResponse.newBuilder()
                .addResults(BalanceResult.newBuilder()
                        .setAmount(usdVal)
                        .setCurrency(Currency.USD))
                .addResults(BalanceResult.newBuilder()
                        .setAmount(eurVal)
                        .setCurrency(Currency.EUR))
                .addResults(BalanceResult.newBuilder()
                        .setAmount(gbpVal)
                        .setCurrency(Currency.GBP))
                .build().getResultsList();
    }

    private void assertBalances(List <BalanceResult> expected, List <BalanceResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (BalanceResult balanceResult : expected) {
            assertTrue(actual.contains(balanceResult));
        }
    }
}

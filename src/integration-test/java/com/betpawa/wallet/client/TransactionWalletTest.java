package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.proto.Wallet;
import com.betpawa.wallet.server.WalletServer;
import com.betpawa.wallet.server.WalletService;
import com.codahale.metrics.MetricRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransactionWalletTest {

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
        WalletConfig config = new WalletConfig();
        config.setServer("localhost");
        config.setPort(port);
        config.setEnableStream(false);
        config.setStoreResponse(true);
        WalletClient client = new WalletClient(config, new MetricRegistry());
        String userId = "1";

        try {
            client.registerUser(userId);
            List<Wallet.Response> expecteds = new ArrayList <>();
            UserRequest userRequest = new UserRequest(userId);

            userRequest.addRequests(userRequest.createWithdrawRequest(200.0, Wallet.Currency.USD));
            expecteds.add(WalletService.INSUFFICIENT_FOUND);

            userRequest.addRequests(userRequest.createDepositRequest(100.0, Wallet.Currency.USD));
            expecteds.add(WalletService.SUCCESS);

            userRequest.addRequests(userRequest.createBalanceRequest());
            expecteds.add(createBalances(100, 0, 0));

            userRequest.addRequests(userRequest.createWithdrawRequest(200.0, Wallet.Currency.USD));
            expecteds.add(WalletService.INSUFFICIENT_FOUND);

            userRequest.addRequests(userRequest.createDepositRequest(100.0, Wallet.Currency.EUR));
            expecteds.add(WalletService.SUCCESS);

            userRequest.addRequests(userRequest.createBalanceRequest());
            expecteds.add(createBalances(100, 100, 0));

            userRequest.addRequests(userRequest.createWithdrawRequest(200.0, Wallet.Currency.USD));
            expecteds.add(WalletService.INSUFFICIENT_FOUND);

            userRequest.addRequests(userRequest.createDepositRequest(100.0, Wallet.Currency.USD));
            expecteds.add(WalletService.SUCCESS);

            userRequest.addRequests(userRequest.createBalanceRequest());
            expecteds.add(createBalances(200, 100, 0));

            userRequest.addRequests(userRequest.createWithdrawRequest(200.0, Wallet.Currency.USD));
            expecteds.add(WalletService.SUCCESS);

            userRequest.addRequests(userRequest.createBalanceRequest());
            expecteds.add(createBalances(0, 100, 0));

            userRequest.addRequests(userRequest.createWithdrawRequest(200.0, Wallet.Currency.USD));
            expecteds.add(WalletService.INSUFFICIENT_FOUND);

            client.sendRequests(userRequest.getRequests());

            while (client.getReceivedCount() < expecteds.size()) {
                Thread.sleep(1000);
            }

            for (int i = 0; i < client.getReceivedCount(); i++) {
                Wallet.Response response = client.removeNextResponse();
                assertEquals(expecteds.get(i), response);
            }
        } finally {
            client.shutdown();
        }
    }

    private Wallet.Response createBalances(double usdVal, double eurVal, double gbpVal) {
        return Wallet.Response.newBuilder()
                .setSuccess(true)
                .addResults(Wallet.BalanceResult.newBuilder()
                        .setAmount(usdVal)
                        .setCurrency(Wallet.Currency.USD))
                .addResults(Wallet.BalanceResult.newBuilder()
                        .setAmount(eurVal)
                        .setCurrency(Wallet.Currency.EUR))
                .addResults(Wallet.BalanceResult.newBuilder()
                        .setAmount(gbpVal)
                        .setCurrency(Wallet.Currency.GBP))
                .build();
    }
}

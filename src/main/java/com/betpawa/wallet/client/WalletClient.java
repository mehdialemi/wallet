package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.betpawa.wallet.commons.Constants.SERVER_PORT;

/**
 * A simple client that send deposit, withdraw, and balance requests to the Wallet server
 */
public class WalletClient {
    private static final Logger logger = LoggerFactory.getLogger(WalletClient.class);

    private final ManagedChannel channel;
    private final WalletTransactionGrpc.WalletTransactionBlockingStub blockingStub;

    /** Construct client connecting to Wallet server at {@code host:port}. */
    WalletClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the wallet test task, I disable TLS
                // to avoid needing certificates.
                .usePlaintext()
                .build());
    }

    /** Construct client for accessing Wallet server using the existing channel. */
    WalletClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = WalletTransactionGrpc.newBlockingStub(channel);
    }

    void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    void deposit(String userId, double amount, Currency currency) {

        DepositRequest request = DepositRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        logger.info( "Will try to deposit {}", StringUtil.toString(request));
        try {
            blockingStub.deposit(request);
        } catch (StatusRuntimeException e) {
            logger.warn(e.getStatus().getDescription());
        }
    }

    void withdraw(String userId, double amount, Currency currency) {
        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        logger.info("Will try to withdraw {}", StringUtil.toString(request));
        try {
            blockingStub.withdraw(request);
        } catch (StatusRuntimeException e) {
            logger.warn(e.getStatus().getDescription());
        }
    }

    void balance(String userId) {
        BalanceRequest request = BalanceRequest.newBuilder()
                .setUserId(userId)
                .build();

        logger.info("Will try to get balance {}", StringUtil.toString(request));
        try {
            BalanceResponse response = blockingStub.balance(request);
            StringBuilder sb = new StringBuilder("Balance: ");
            for (BalanceResult result : response.getResultsList()) {
                sb.append(result.getAmount())
                        .append(" ")
                        .append(result.getCurrency().name())
                        .append(", ");
            }
            logger.info(sb.toString());
        } catch (StatusRuntimeException e) {
            logger.warn(e.getStatus().getDescription());
        }
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        WalletClient client = new WalletClient("localhost", SERVER_PORT);
        try {
            /* Access a service running on the local machine on port 50051 */
            String userId = "1";

            client.withdraw(userId, 200.0, Currency.USD);
            client.deposit(userId, 100.0, Currency.USD);
            client.balance(userId);

            client.withdraw(userId, 200.0, Currency.USD);
            client.deposit(userId, 100.0, Currency.EUR);
            client.balance(userId);

            client.withdraw(userId, 200.0, Currency.USD);
            client.deposit(userId, 100.0, Currency.USD);
            client.balance(userId);

            client.withdraw(userId, 200.0, Currency.USD);
            client.balance(userId);

            client.withdraw(userId, 200.0, Currency.USD);
        } finally {
            client.shutdown();
        }
    }

}

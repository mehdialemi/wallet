package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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

        logger.info( "Will try to deposit {}", request);
        try {
            blockingStub.deposit(request);
        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {0}", e.getStatus(), e);
        } catch (Throwable t) {
            logger.error("Error: {}", t.getMessage());
        }
    }

    void withdraw(String userId, double amount, Currency currency) {
        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        logger.info("Will try to withdraw {}", request);
        try {
            blockingStub.withdraw(request);
        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {0}", e.getStatus(), e);
        } catch (Throwable t) {
            logger.error("Error: {}", t.getMessage());
        }
    }

    void balance(String userId) {
        BalanceRequest request = BalanceRequest.newBuilder()
                .setUserId(userId)
                .build();

        logger.info("Will try to get balance {}", request);
        try {
            blockingStub.balance(request);
        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {0}", e.getStatus(), e);
        } catch (Throwable t) {
            logger.error("Error: {}", t.getMessage());
        }
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        WalletClient client = new WalletClient("localhost", 50051);
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

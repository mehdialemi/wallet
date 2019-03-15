package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
    public WalletClient(String host, int port) {
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

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String createAccount(String userId) {
        NewAccount account = NewAccount.newBuilder()
                .setUserId(userId)
                .build();

        try {
            blockingStub.createAccount(account);
            return "ok";
        } catch (StatusRuntimeException e) {
            String msg = e.getStatus().getDescription();
            logger.debug(msg);
            return msg;
        }
    }

    public String deposit(String userId, double amount, Currency currency) {

        DepositRequest request = DepositRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        logger.trace( "Will try to deposit {}", StringUtil.toString(request));
        try {
            blockingStub.deposit(request);
            return "ok";
        } catch (StatusRuntimeException e) {
            String msg = e.getStatus().getDescription();
            logger.debug(msg);
            return msg;
        }
    }

    public String withdraw(String userId, double amount, Currency currency) {
        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        logger.trace("Will try to withdraw {}", StringUtil.toString(request));
        try {
            blockingStub.withdraw(request);
            return "ok";
        } catch (StatusRuntimeException e) {
            String msg = e.getStatus().getDescription();
            logger.debug(msg);
            return msg;
        }
    }

    public BalanceResponse balance(String userId) {
        BalanceRequest request = BalanceRequest.newBuilder()
                .setUserId(userId)
                .build();

        logger.trace("Will try to get balance {}", StringUtil.toString(request));
        try {
            BalanceResponse response = blockingStub.balance(request);
            StringBuilder sb = new StringBuilder("Balance: ");
            for (BalanceResult result : response.getResultsList()) {
                sb.append(result.getAmount())
                        .append(" ")
                        .append(result.getCurrency().name())
                        .append(", ");
            }
            logger.trace(sb.toString());
            return response;
        } catch (StatusRuntimeException e) {
            logger.debug(e.getStatus().getDescription());
            return null;
        }
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        List<WalletUser> walletUsers = new ArrayList <>();
        try {
            /* Access a service running on the local machine on port 50051 */

            String server = "localhost";
            int port = SERVER_PORT;

            int numUsers = 100;
            int numThreads = 10;
            int numRounds = 100;

            for (int i = 0; i < numUsers; i++) {
                walletUsers.add(new WalletUser(server, port, "userId-" + i, numThreads, numRounds));
            }

            for (WalletUser walletUser : walletUsers) {
                walletUser.start();
            }

            for (WalletUser walletUser : walletUsers) {
                walletUser.waitToComplete();
            }

        } finally {
            for (WalletUser walletUser : walletUsers) {
                walletUser.close();
            }

            logger.info("WalletClient is down");
        }
    }

}

package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.StringUtil;
import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.proto.*;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple client that send deposit, withdraw, and balance requests to the Wallet server
 */
public class WalletClient {
    private static final Logger logger = LoggerFactory.getLogger(WalletClient.class);

    private final ManagedChannel channel;
    private final WalletTransactionGrpc.WalletTransactionBlockingStub blockingStub;
    private final WalletTransactionGrpc.WalletTransactionStub asyncStub;
    private ResultProvider resultProvider;

    /** Construct client connecting to Wallet server at {@code host:port}. */
    public WalletClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the wallet test task, I disable TLS
                // to avoid needing certificates.
                .usePlaintext()
                .build());
    }

    /** Construct client for accessing Wallet server using the existing channel. */
    public WalletClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = WalletTransactionGrpc.newBlockingStub(channel);
        asyncStub = WalletTransactionGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String registerUser(String userId) {
        UserId user = UserId.newBuilder()
                .setUserId(userId)
                .build();

        try {
            blockingStub.registerUser(user);
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

    public void sendAllRequests(List<ClientRequest> requests) {

        StreamObserver<BalanceResponse> responseObserver = new StreamObserver <BalanceResponse>() {
            @Override
            public void onNext(BalanceResponse value) {
                if (resultProvider != null) {
                    resultProvider.onResponse(value);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (resultProvider != null) {
                    resultProvider.onRpcError(t);
                }
            }

            @Override
            public void onCompleted() {
                logger.info("Request completed");
            }
        };

        StreamObserver <ClientRequest> call = asyncStub.call(responseObserver);
        try {
            for (ClientRequest request : requests) {
                call.onNext(request);
            }
        } catch (Throwable t) {
            call.onError(t);
        }

        call.onCompleted();
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

        String optNumUsers = "u";
        String optNumThreads = "t";
        String optNumRounds = "r";
        String optConfig = "c";
        Options options = new Options();
        options.addOption(optConfig, true, "config file address, default is wallet.properties" +
                " in the current directory");
        options.addOption(optNumUsers, true, "number of users, default is 1");
        options.addOption(optNumThreads, true, "number of threads per user, default is 1");
        options.addOption(optNumRounds, true, "number of rounds per user, default is 1");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String configFile = cmd.getOptionValue(optConfig, "wallet.properties");
        int numUsers = Integer.valueOf(cmd.getOptionValue(optNumUsers, "1"));
        int numThreads = Integer.valueOf(cmd.getOptionValue(optNumThreads, "1"));
        int numRounds = Integer.valueOf(cmd.getOptionValue(optNumRounds, "1"));

        WalletConfig config = new WalletConfig(configFile);
        List<WalletUser> walletUsers = new ArrayList <>();
        logger.info("Starting client with config, users: {}, perUserThreads: {}, perUserRounds: {}",
                numUsers, numThreads, numRounds);
        logger.info("Connecting users server {}:{}", config.getServer(), config.getPort());

        MetricRegistry metricRegistry = new MetricRegistry();
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.SECONDS)
                .shutdownExecutorOnStop(true)
                .build();
        reporter.start(config.getReportPeriodSec(), TimeUnit.SECONDS);

        try {
            /* Access a service running on the local machine on port 50051 */

            for (int i = 0; i < numUsers; i++) {
                walletUsers.add(new WalletUser(config.getServer(), config.getPort(), "uid-" + i, numThreads, numRounds, metricRegistry));
            }

            for (WalletUser walletUser : walletUsers) {
                walletUser.start();
            }

            for (WalletUser walletUser : walletUsers) {
                walletUser.waitToComplete();
            }

//            System.out.println("Enter string to exit");
//            Scanner scanner = new Scanner(System.in);
//            String line = scanner.nextLine();
            reporter.report();
        } finally {
            for (WalletUser walletUser : walletUsers) {
                walletUser.close();
            }

            logger.info("WalletClient is down");
        }
    }

    interface ResultProvider {
        /**
         * Used for verify/inspect message received from server.
         */
        void onResponse(BalanceResponse message);

        /**
         * Used for verify/inspect error received from server.
         */
        void onRpcError(Throwable exception);
    }

    void setResultProvider(ResultProvider resultProvider) {
        this.resultProvider = resultProvider;
    }

}

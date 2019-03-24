package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.proto.Wallet;
import com.betpawa.wallet.proto.WalletTransactionGrpc;
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

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple client that send deposit, withdraw, and balance requests to the Wallet server
 */
public class WalletClient {
    private static final Logger logger = LoggerFactory.getLogger(WalletClient.class);
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    public static final String ROUND_OPERATION_DELAY = "round.operation.delay";

    private final ManagedChannel channel;
    private MetricRegistry metricRegistry;
    private final WalletTransactionGrpc.WalletTransactionBlockingStub blockingStub;
    private final WalletTransactionGrpc.WalletTransactionStub asyncStub;
    private final Queue <Wallet.Response> rQueue;
    private WalletConfig config;
    private final AtomicInteger received = new AtomicInteger();
    private final AtomicInteger sent = new AtomicInteger();
    private final Semaphore semaphore = new Semaphore(0);

    /**
     * Construct client connecting to Wallet server at {@code host:port}.
     */
    WalletClient(WalletConfig config, MetricRegistry metricRegistry) {
        this.channel = ManagedChannelBuilder.forAddress(config.getServer(), config.getPort())
                // Channels are secure by default (via SSL/TLS). For the wallet test task, I disable TLS
                // to avoid needing certificates.
                .usePlaintext()
                .build();
        this.metricRegistry = metricRegistry;
        this.blockingStub = WalletTransactionGrpc.newBlockingStub(channel);
        this.asyncStub = WalletTransactionGrpc.newStub(channel);
        this.rQueue = new ConcurrentLinkedDeque <>();
        this.config = config;
    }

    public WalletConfig getConfig() {
        return config;
    }

    void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    void registerUser(String userId) {
        Wallet.UserId user = Wallet.UserId.newBuilder()
                .setUserId(userId)
                .build();
        try {
            blockingStub.registerUser(user);
        } catch (StatusRuntimeException e) {
            String msg = e.getStatus().getDescription();
            logger.debug(msg);
        }
    }

    Wallet.Response removeNextResponse() {
        if (config.isStoreResponse()) {
            return rQueue.remove();
        }
        return null;
    }

    void waitToComplete() throws Exception {
        semaphore.acquire(sent.get());
    }

    int getReceivedCount() {
        return received.get();
    }

    int getSendCount() {
        return sent.get();
    }
    private double receivedRatio() {
        return received.get() / (double) sent.get();
    }

    void sendRequests(List <Wallet.Request> requests) {

        StreamObserver <Wallet.Response> responseObserver = new StreamObserver <Wallet.Response>() {
            @Override
            public void onNext(Wallet.Response response) {
                received.incrementAndGet();
                semaphore.release();
                if (config.isStoreResponse()) {
                    rQueue.add(response);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error in client", t);
            }

            @Override
            public void onCompleted() {
                logger.debug("Request completed");
            }
        };

        StreamObserver <Wallet.Request> streamCall = asyncStub.streamCall(responseObserver);
        try {
            for (Wallet.Request request : requests) {
                if (config.isEnableStream()) {
                    streamCall.onNext(request);
                    sent.incrementAndGet();
                } else {
                    com.codahale.metrics.Timer.Context context = metricRegistry.timer(ROUND_OPERATION_DELAY).time();
                    Wallet.Response response = blockingStub.call(request);
                    context.stop();
                    sent.incrementAndGet();
                    semaphore.release();
                    received.incrementAndGet();
                    if (config.isStoreResponse()) {
                        rQueue.add(response);
                    }
                }
            }
        } catch (Throwable t) {
            streamCall.onError(t);
        }

        if (config.isEnableStream()) {
            streamCall.onCompleted();
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
        final List <WalletUser> walletUsers = new ArrayList <>();
        logger.info("Starting client with config, users: {}, perUserThreads: {}, perUserRounds: {}",
                numUsers, numThreads, numRounds);
        logger.info("Connecting users server {}:{}", config.getServer(), config.getPort());

        MetricRegistry metricRegistry = new MetricRegistry();

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.SECONDS)
                .shutdownExecutorOnStop(true)
                .build();
        if (!config.isEnableStream()) {
            reporter.start(config.getReportPeriodSec(), TimeUnit.SECONDS);
        }

        Timer timer = new Timer();

        try {
            /* Access a service running on the local machine on port 50051 */

            for (int i = 0; i < numUsers; i++) {
                walletUsers.add(new WalletUser(new WalletClient(config, metricRegistry), "uid-" + i, numThreads, numRounds));
            }

            for (WalletUser walletUser : walletUsers) {
                walletUser.start();
            }

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    double received = 0.0;
                    int completed = 0;
                    for (WalletUser walletUser : walletUsers) {
                        double ratio = walletUser.getWalletClient().receivedRatio();
                        if (ratio >= 100.0)
                            completed++;
                        received += ratio;
                    }

                    double ratio = received / walletUsers.size();
                    logger.info("Users: {}, completed requests: {}, received ratio: {}"
                            , walletUsers.size(), completed, DF.format(ratio));
                }
            };

            timer.schedule(timerTask, 0, config.getReportPeriodSec() * 1000);

            for (WalletUser walletUser : walletUsers) {
                walletUser.waitToComplete();
            }

            if (!config.isEnableStream()) {
                reporter.report();
            }

            timerTask.run();
        } finally {
            for (WalletUser walletUser : walletUsers) {
                walletUser.close();
            }

            timer.cancel();
            logger.info("WalletClient is down");
        }
    }

}

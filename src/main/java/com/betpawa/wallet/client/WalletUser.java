package com.betpawa.wallet.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WalletUser implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(WalletUser.class);
    private final WalletClient walletClient;
    private final ExecutorService executorService;
    private final ArrayList <Callable<Long>> roundList;
    private String userId;
    private final List <Future <Long>> futures = new ArrayList <>();

    WalletUser(String server, int port, String userId, int threads, int threadRounds, MetricRegistry registry) {
        this.userId = userId;

        this.walletClient = new WalletClient(server, port);
        logger.info("registering userId: {}", userId);
        walletClient.registerUser(userId);

        executorService = Executors.newFixedThreadPool(threads);
        roundList = new ArrayList <>();

        final Timer roundOperationTimer = registry.timer("round.operation.delay");
        UserRound.setOperationTimer(roundOperationTimer);


        for (int thread = 0; thread < threads; thread++) {
            roundList.add(() -> {
                long start = System.currentTimeMillis();
                for (int round = 0; round < threadRounds; round++) {
                    UserRound.randomRound(walletClient, userId);
                }
                Long duration = System.currentTimeMillis() - start;
                return duration;
            });
        }
    }

    public String getUserId() {
        return userId;
    }

    public void start() {
        logger.info("Starting wallet client for userId: {}", userId);
        for (Callable <Long> callable : roundList) {
            futures.add(executorService.submit(callable));
        }
    }

    void waitToComplete() throws Exception {
        long sum = 0;
        for (Future <Long> future : futures) {
            sum += future.get();
        }
        double avg = sum / (double) futures.size();
        logger.info("Round for userId {} is completed, average response time: {}", userId, avg);
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (walletClient != null)
                walletClient.shutdown();
        } catch (InterruptedException e) {
            logger.error("Error to shutdown wallet client", e);
        }
    }
}

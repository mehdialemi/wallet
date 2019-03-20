package com.betpawa.wallet.client;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WalletUser implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(WalletUser.class);
    private final WalletClient walletClient;
    private final ExecutorService executorService;
    private final ArrayList <Callable<Boolean>> roundList;
    private String userId;
    private final List <Future <Boolean>> futures = new ArrayList <>();

    public WalletUser(String server, int port, String userId, int threads, int threadRounds, MetricRegistry registry) {
        this.userId = userId;

        this.walletClient = new WalletClient(server, port);
        executorService = Executors.newFixedThreadPool(threads);
        roundList = new ArrayList <>();

        walletClient.createAccount(userId);
        final Timer roundTimer = registry.timer("user.rounds.delay");
        final Timer roundOperationTimer = registry.timer("round.operation.delay");
        UserRound.setOperationTimer(roundOperationTimer);

        for (int thread = 0; thread < threads; thread++) {
            roundList.add(() -> {
                for (int round = 0; round < threadRounds; round++) {
                    Timer.Context time = roundTimer.time();
                    UserRound.randomRound(walletClient, userId);
                    time.stop();
                }
                return true;
            });
        }
    }

    public String getUserId() {
        return userId;
    }

    public void start() throws Exception {
        logger.info("Starting wallet client for userId: {}", userId);
        for (Callable <Boolean> callable : roundList) {
            futures.add(executorService.submit(callable));
        }
    }

    public void waitToComplete() throws Exception {
        int success = 0;
        int failed = 0;
        for (Future <Boolean> future : futures) {
            if (future.get()) success ++;
            else failed ++;
        }
        logger.info("Round for userId {} is completed, success: {}, failed: {}", userId, success, failed);
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

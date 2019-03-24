package com.betpawa.wallet.client;

import com.betpawa.wallet.commons.WalletConfig;
import com.betpawa.wallet.proto.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    private final UserRequest userRequest;
    private final Random random = new Random();

    WalletUser(WalletClient walletClient, String userId, int threads, int threadRounds) {
        this.userId = userId;
        userRequest = new UserRequest(userId);

        this.walletClient = walletClient;

        logger.info("registering userId: {}", userId);
        walletClient.registerUser(userId);

        executorService = Executors.newFixedThreadPool(threads);
        roundList = new ArrayList <>();

        for (int thread = 0; thread < threads; thread++) {
            roundList.add(() -> {
                long start = System.currentTimeMillis();
                for (int round = 0; round < threadRounds; round++) {
                    List<Wallet.Request> requests = null;
                    switch (random.nextInt(3)) {
                        case 0: requests = userRequest.roundA(); break;
                        case 1: requests = userRequest.roundB(); break;
                        case 2: requests = userRequest.roundC(); break;
                    }

                    walletClient.sendRequests(requests);
                }
                return System.currentTimeMillis() - start;
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

    public WalletClient getWalletClient() {
        return walletClient;
    }

    void waitToComplete() throws Exception {

        long sum = 0;
        for (Future <Long> future : futures) {
            sum += future.get();
        }

        walletClient.waitToComplete();
        logger.debug("Completed responses for userId: {}, sent: {}, received: {}",
                userId, walletClient.getSendCount(), walletClient.getReceivedCount());

        if (!walletClient.getConfig().isEnableStream()) {
            double avg = sum / (double) futures.size();
            logger.debug("Average response time for userId {} is {}", userId, avg);
        }
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

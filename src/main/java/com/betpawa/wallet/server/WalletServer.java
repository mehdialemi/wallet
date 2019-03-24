package com.betpawa.wallet.server;

import com.betpawa.wallet.commons.HibernateUtil;
import com.betpawa.wallet.commons.WalletConfig;
import io.grpc.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server that manages startup/shutdown of a {@code Wallet} server.
 */
public class WalletServer {

    private static final Logger logger = LoggerFactory.getLogger(WalletServer.class);

    private Server server;
    private WalletConfig walletConfig;

    public WalletServer(WalletConfig config) {

        this.walletConfig = config;
    }

    public void start() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(walletConfig.getServerThreads());

        /* The port on which the server should run */
        CompressorRegistry compressorRegistry = CompressorRegistry.getDefaultInstance();
        DecompressorRegistry decompressorRegistry = DecompressorRegistry.getDefaultInstance();
        server = ServerBuilder.forPort(walletConfig.getPort())
                .compressorRegistry(compressorRegistry)
                .decompressorRegistry(decompressorRegistry)
                .addService(new WalletService(walletConfig))
                .executor(executorService)
                .build()
                .start();
        logger.info("Server started, listening on " + walletConfig.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down wallet server");

            System.err.println("*** closing hibernate...");
            HibernateUtil.shutdown();

            System.err.println("*** closing server...");
            WalletServer.this.stop();

            System.err.println("*** server shut down");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws Exception {
        String optConfig = "c";
        Options options = new Options();
        options.addOption(optConfig, true, "config file address, default is wallet.properties" +
                " in the current directory");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String configFile = cmd.getOptionValue(optConfig, "wallet.properties");

        final WalletServer server = new WalletServer(new WalletConfig(configFile));
        server.start();
        server.blockUntilShutdown();
    }
}

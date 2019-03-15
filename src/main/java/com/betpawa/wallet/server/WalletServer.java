package com.betpawa.wallet.server;

import com.betpawa.wallet.commons.Constants;
import com.betpawa.wallet.commons.HibernateUtil;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Server that manages startup/shutdown of a {@code Wallet} server.
 */
public class WalletServer {

    private static final Logger logger = LoggerFactory.getLogger(WalletServer.class);

    private Server server;

    public void start(int port) throws IOException {
        /* The port on which the server should run */
        server = ServerBuilder.forPort(port)
                .addService(new WalletServerImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + Constants.SERVER_PORT);

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
    public static void main(String[] args) throws IOException, InterruptedException {
        final WalletServer server = new WalletServer();
        server.start(Constants.SERVER_PORT);
        server.blockUntilShutdown();
    }
}

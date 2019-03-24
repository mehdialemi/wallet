package com.betpawa.wallet.commons;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class WalletConfig {

    private String server = "localhost";
    private int port = 8080;
    private int reportPeriodSec = 10;
    private int serverThreads = 5;
    private boolean enableStream = true;
    private boolean storeResponse = false;

    public WalletConfig() {
    }

    public WalletConfig(String file) throws ConfigurationException {
        PropertiesConfiguration properties = new Configurations().properties(new File(file));
        server = properties.getString("server.host", "localhost");
        port = properties.getInt("server.port", 80080);
        reportPeriodSec = properties.getInt("server.report.period.second", 10);
        serverThreads = properties.getInt("server.threads", 5);
        enableStream = properties.getBoolean("client.stream.enable", true);
        storeResponse = properties.getBoolean("client.response.store", false);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReportPeriodSec() {
        return reportPeriodSec;
    }

    public void setReportPeriodSec(int reportPeriodSec) {
        this.reportPeriodSec = reportPeriodSec;
    }

    public int getServerThreads() {
        return serverThreads;
    }

    public void setServerThreads(int serverThreads) {
        this.serverThreads = serverThreads;
    }

    public boolean isEnableStream() {
        return enableStream;
    }

    public void setEnableStream(boolean enableStream) {
        this.enableStream = enableStream;
    }

    public boolean isStoreResponse() {
        return storeResponse;
    }

    public void setStoreResponse(boolean storeResponse) {
        this.storeResponse = storeResponse;
    }
}

package com.betpawa.wallet.client;

import com.betpawa.wallet.proto.Wallet;

import java.util.ArrayList;
import java.util.List;

public class UserRequest {

    private final List<Wallet.Request> requestsA;
    private final List<Wallet.Request> requestsB;
    private final List<Wallet.Request> requestsC;
    private final List<Wallet.Request> requests;
    private String userId;

    UserRequest(String userId) {
        this.userId = userId;
        requests = new ArrayList <>();

        requestsA = new ArrayList <>();
        requestsA.add(createDepositRequest(100, Wallet.Currency.USD));
        requestsA.add(createWithdrawRequest( 200, Wallet.Currency.USD));
        requestsA.add(createDepositRequest( 100, Wallet.Currency.EUR));
        requestsA.add(createBalanceRequest());
        requestsA.add(createWithdrawRequest(100, Wallet.Currency.USD));
        requestsA.add(createBalanceRequest());
        requestsA.add(createWithdrawRequest(100, Wallet.Currency.USD));

        requestsB = new ArrayList <>();
        requestsB.add(createWithdrawRequest( 100, Wallet.Currency.GBP));
        requestsB.add(createDepositRequest( 300, Wallet.Currency.GBP));
        requestsB.add(createWithdrawRequest(100, Wallet.Currency.GBP));
        requestsB.add(createWithdrawRequest(100, Wallet.Currency.GBP));
        requestsB.add(createWithdrawRequest(100, Wallet.Currency.GBP));

        requestsC = new ArrayList <>();
        requestsC.add(createBalanceRequest());
        requestsC.add(createDepositRequest( 100, Wallet.Currency.USD));
        requestsC.add(createDepositRequest( 100, Wallet.Currency.USD));
        requestsC.add(createWithdrawRequest( 100, Wallet.Currency.USD));
        requestsC.add(createDepositRequest(100, Wallet.Currency.USD));
        requestsC.add(createBalanceRequest());
        requestsC.add(createWithdrawRequest( 200, Wallet.Currency.USD));
        requestsC.add(createBalanceRequest());
    }

    void addRequests(Wallet.Request request) {
        requests.add(request);
    }

    public List<Wallet.Request> getRequests() {
        return requests;
    }

    List<Wallet.Request> roundA() {
        return requestsA;
    }

    List<Wallet.Request> roundB() {
        return requestsB;
    }

    List<Wallet.Request> roundC() {
        return requestsC;
    }

    Wallet.Request createDepositRequest(double amount, Wallet.Currency currency) {
        return Wallet.Request.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .setOp(Wallet.Operation.DEPOSIT)
                .build();
    }

    Wallet.Request createWithdrawRequest(double amount, Wallet.Currency currency) {
        return Wallet.Request.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .setOp(Wallet.Operation.WITHDRAW)
                .build();
    }

    Wallet.Request createBalanceRequest() {
        return Wallet.Request.newBuilder()
                .setUserId(userId)
                .setOp(Wallet.Operation.BALANCE)
                .build();
    }


}

package com.betpawa.wallet.commons;

public class StringUtil {

    public static String toString(WithdrawRequest request) {
        return "(userId: " + request.getUserId() + ", amount: " + request.getAmount() + ", currency: " + request.getCurrency() + ")";
    }

    public static String toString(DepositRequest request) {
        return "(userId: " + request.getUserId() + ", amount: " + request.getAmount() + ", currency: " + request.getCurrency() + ")";
    }

    public static String toString(BalanceRequest request) {
        return "(userId: " + request.getUserId() + ")";
    }
}

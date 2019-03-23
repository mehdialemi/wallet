package com.betpawa.wallet.commons;

import com.betpawa.wallet.proto.BalanceRequest;
import com.betpawa.wallet.proto.DepositRequest;
import com.betpawa.wallet.proto.WithdrawRequest;

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

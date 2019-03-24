package com.betpawa.wallet.commons;

import com.betpawa.wallet.proto.*;

public class StringUtil {

    public static String toString(Wallet.Request request) {
        return "(userId: " + request.getUserId() + ", amount: " + request.getAmount() +
                ", currency: " + request.getCurrency() + ")";
    }

    public static String toString(Wallet.Response response) {
        StringBuilder sb = new StringBuilder("[");
        sb.append("success: ").append(response.getSuccess()).append(", ");
        sb.append("message: ").append(response.getMessage()).append("] ");
        if (response.getResultsCount() > 0) {
            for (Wallet.BalanceResult balanceResult : response.getResultsList()) {
                sb.append(balanceResult.getAmount()).append(balanceResult.getCurrency());
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}

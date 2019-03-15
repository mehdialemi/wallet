package com.betpawa.wallet.exceptions;

public class InSufficientFundException extends RuntimeException {

    InSufficientFundException() {
        super("insufficient_funds");
    }
}

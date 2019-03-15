package com.betpawa.wallet.exceptions;

public class InSufficientFundException extends RuntimeException {

    public InSufficientFundException() {
        super("insufficient_funds");
    }
}

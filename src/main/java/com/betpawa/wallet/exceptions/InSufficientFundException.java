package com.betpawa.wallet.exceptions;

public class InSufficientFundException extends RuntimeException {

    public static final String MESSAGE = "insufficient_funds";

    public InSufficientFundException() {
        super(MESSAGE);
    }
}

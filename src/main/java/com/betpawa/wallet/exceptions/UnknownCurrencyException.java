package com.betpawa.wallet.exceptions;

public class UnknownCurrencyException extends RuntimeException {

    public static final String MESSAGE = "unknown_currency";

    public UnknownCurrencyException() {
        super(MESSAGE);
    }
}

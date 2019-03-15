package com.betpawa.wallet.exceptions;

public class UnknownCurrencyException extends RuntimeException {

    public UnknownCurrencyException() {
        super("unknown_currency");
    }
}

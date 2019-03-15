package com.betpawa.wallet.services;

import com.betpawa.wallet.commons.Currency;
import com.betpawa.wallet.repository.Account;
import com.betpawa.wallet.commons.BalanceRequest;
import com.betpawa.wallet.commons.DepositRequest;
import com.betpawa.wallet.commons.WithdrawRequest;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;

public class AccountService {

    public Account getUserAccount(String userId) {
        return new Account();
    }

    public void withdraw(WithdrawRequest withdrawRequest) throws InSufficientFundException {
        validateOrThrowRunnable(withdrawRequest.getAmount(), withdrawRequest.getCurrency());
    }

    public void deposite(DepositRequest depositRequest) {
        validateOrThrowRunnable(depositRequest.getAmount(), depositRequest.getCurrency());
    }

    public Account balance(BalanceRequest balanceRequest) {
        return new Account();
    }


    private void validateOrThrowRunnable(double amount, Currency currency) {
        if (amount < 0) {
            throw new IllegalArgumentException(String.format("Invalid deposit amount %s", amount));
        }

        if (currency == Currency.UNRECOGNIZED)
            throw new UnknownCurrencyException();

    }
}

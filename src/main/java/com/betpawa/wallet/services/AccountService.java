package com.betpawa.wallet.services;

import com.betpawa.wallet.commons.Currency;
import com.betpawa.wallet.commons.DepositRequest;
import com.betpawa.wallet.commons.WithdrawRequest;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import com.betpawa.wallet.repository.Account;
import com.betpawa.wallet.repository.Balance;
import com.betpawa.wallet.repository.Transaction;
import com.betpawa.wallet.repository.TransactionType;
import org.hibernate.Session;

import java.util.Date;

public class AccountService {

    public void newAccount(String userId) {
        validateOrThrowRunnable(userId);

        try(MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            Account account = new Account();
            account.setUserId(userId);
            session.save(account);
        }
    }

    public void withdraw(WithdrawRequest request) throws InSufficientFundException {
        validateOrThrowRunnable(request.getUserId(), request.getAmount(), request.getCurrency());

        // First, open a new session and then begin a transaction
        try(MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();

            Account account = getAccount(request.getUserId(), session);
            Balance balance = getCurrencyBalance(account, request.getCurrency());

            if (balance.getAmount() < request.getAmount())
                throw new InSufficientFundException();

            balance.setAmount(balance.getAmount() - request.getAmount());

            Transaction transaction = new Transaction();
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setDate(new Date());
            transaction.setBalance(balance);

            session.save(balance);
            session.save(account);
            session.save(transaction);
        } // Finally, commit the transaction and close the session
    }

    public void deposit(DepositRequest request) {
        validateOrThrowRunnable(request.getUserId(), request.getAmount(), request.getCurrency());

        // First, open a new session and then begin a transaction
        try(MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();

            Account account = getAccount(request.getUserId(), session);
            Balance balance = getCurrencyBalance(account, request.getCurrency());
            balance.setAmount(balance.getAmount() + request.getAmount());

            Transaction transaction = new Transaction();
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDate(new Date());
            transaction.setBalance(balance);

            session.save(balance);
            session.save(account);
            session.save(transaction);
        } // Finally, commit the transaction and close the session
    }

    /**
     * @return retrieve the account information for the given userId.
     * If userId is null, an IllegalArgumentException would be thrown
     */
    public Account getAccount(String userId) {
        validateOrThrowRunnable(userId);

        // First, open a new session and then begin a transaction
        try(MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            return getAccount(userId, session);
        } // Finally, commit the transaction and close the session
    }

    private Account getAccount(String userId, Session session) {
        @SuppressWarnings("JpaQlInspection") Object result = session
                .createQuery("from Account where userId=:userId")
                .setParameter("userId", userId)
                .setFetchSize(1)
                .uniqueResult();

        Account account;
        if (result == null) {
                account = new Account();
                account.setUserId(userId);
                session.save(account);
        } else {
            account = (Account) result;
        }
        return account;
    }

    /**
     * @return
     * Regarding the input currency, return the corresponding balance of the given account.
     * When no related balance could be found, a new Balance instance with the given Currency
     * would be createdUnknownCurrencyException will be thrown.
     */
    private Balance getCurrencyBalance(Account account, Currency currency) {
        Balance balance = null;
        for (Balance currentBalance : account.getBalance()) {
            if (currentBalance.getCurrency() == currency) {
                balance = currentBalance;
                break;
            }
        }

        if (balance == null) {
            balance = new Balance();
            balance.setCurrency(currency);
            account.getBalance().add(balance);
        }

        return balance;
    }

    /**
     * Check userId is not null; otherwise throws IllegalArgumentException
     * Check amount > 0; otherwise throws IllegalArgumentException
     * Recognize the given currency; otherwise throws UnknownCurrencyException
     */
    private void validateOrThrowRunnable(String userId, double amount, Currency currency) {
        validateOrThrowRunnable(userId);

        if (amount < 0) {
            throw new IllegalArgumentException(String.format("Invalid deposit amount %s", amount));
        }

        if (currency == Currency.UNRECOGNIZED)
            throw new UnknownCurrencyException();

    }

    private void validateOrThrowRunnable(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User Id is null!");
        }
    }
}

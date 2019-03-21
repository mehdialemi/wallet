package com.betpawa.wallet.account;

import com.betpawa.wallet.commons.*;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;

public class AccountService {

    public void register(String userId) {
        validateOrThrowRunnable(userId);

        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            if (AccountRepository.accountExists(session, userId))
                return;

            session.save(Account.create(userId, Currency.USD));
            session.save(Account.create(userId, Currency.EUR));
            session.save(Account.create(userId, Currency.GBP));
            session.flush();
        }
    }

    public void withdraw(WithdrawRequest request) throws InSufficientFundException {
        validateOrThrowRunnable(request.getUserId(), request.getAmount(), request.getCurrency());

        // First, open a new session and then begin a transaction
        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();

            Account account = AccountRepository.findByUserIdCurrency(session, request.getUserId(), request.getCurrency());
            if (account == null)
                throw new InSufficientFundException();

            if (account.getAmount() < request.getAmount())
                throw new InSufficientFundException();

            account.decrease(request.getAmount());
            session.save(account);

            Transaction transaction = new Transaction();
            transaction.setDate(new Date());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setAccount(account);
            session.persist(transaction);
        } // Finally, commit the transaction and close the session
    }

    public void deposit(DepositRequest request) {
        validateOrThrowRunnable(request.getUserId(), request.getAmount(), request.getCurrency());

        // First, open a new session and then begin a transaction
        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            Account account = AccountRepository.findByUserIdCurrency(session, request.getUserId(), request.getCurrency());

            if (account == null)
                throw new InSufficientFundException();

            account.increase(request.getAmount());
            session.save(account);

            Transaction transaction = new Transaction();
            transaction.setDate(new Date());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setAccount(account);
            session.persist(transaction);
        } // Finally, commit the transaction and close the session
    }

    /**
     * @return retrieve the account information for the given userId.
     * If userId is null, an IllegalArgumentException would be thrown
     */
    public List <Account> getAccount(String userId) {
        validateOrThrowRunnable(userId);

        // First, open a new session and then begin a transaction
        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            return AccountRepository.findByUserId(session, userId);
        } // Finally, commit the transaction and close the session
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

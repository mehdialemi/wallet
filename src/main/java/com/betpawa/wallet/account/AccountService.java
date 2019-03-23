package com.betpawa.wallet.account;

import com.betpawa.wallet.commons.MySession;
import com.betpawa.wallet.commons.TransactionType;
import com.betpawa.wallet.exceptions.InSufficientFundException;
import com.betpawa.wallet.exceptions.UnknownCurrencyException;
import com.betpawa.wallet.proto.Currency;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;

public class AccountService {

    private AccountRepository accountRepository;
    public AccountService() {
        accountRepository = new AccountRepository();
    }

    public void register(String userId) {
        validateOrThrowRunnable(userId);

        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
            if (accountRepository.accountExists(session, userId))
                return;

            createAccount(userId, session);
        }
    }

    private void createAccount(String userId, Session session) {
//        session.setCacheMode(CacheMode.PUT);
        session.save(Account.create(userId, Currency.USD));
        session.save(Account.create(userId, Currency.EUR));
        session.save(Account.create(userId, Currency.GBP));
    }

    public void withdraw(String userId, double amount, Currency currency) throws InSufficientFundException {
        validateOrThrowRunnable(userId, amount, currency);

        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();

//            session.setCacheMode(CacheMode.GET);
            Account account = accountRepository.findByUserIdCurrency(session, userId, currency);
            if (account == null) {
                createAccount(userId, session);
                throw new InSufficientFundException();
            }

            if (account.getAmount() < amount)
                throw new InSufficientFundException();

            account.decrease(amount);
//            session.setCacheMode(CacheMode.PUT);
            session.save(account);

            Transaction transaction = new Transaction();
            transaction.setDate(new Date());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setAccount(account);
            session.persist(transaction);
        } // Finally, commit the transaction and close the session
    }

    public void deposit(String userId, double amount, Currency currency) {
        validateOrThrowRunnable(userId, amount, currency);

        // First, open a new session and then begin a transaction
        try (MySession mySession = MySession.newSession()) {
            Session session = mySession.openSession();
//            session.setCacheMode(CacheMode.GET);

            Account account = accountRepository.findByUserIdCurrency(session, userId, currency);

            if (account == null) {
                createAccount(userId, session);
                throw new InSufficientFundException();
            }


            account.increase(amount);
//            session.setCacheMode(CacheMode.PUT);

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
            session.setDefaultReadOnly(true);
            return accountRepository.findByUserId(session, userId);
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

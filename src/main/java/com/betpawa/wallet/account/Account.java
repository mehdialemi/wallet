package com.betpawa.wallet.account;

import com.betpawa.wallet.proto.Wallet;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@NamedNativeQueries({
        @NamedNativeQuery(name="find_by_user_id",
                query = "select * from accounts where user_id=:userId",
                resultClass = Account.class),
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Account implements Serializable {

    @EmbeddedId
    private AccountPK accountPK;

    @Column
    private double amount;

    @OneToMany
    private Set<Transaction> transactions = new HashSet <>();

    public static Account create(String userId, Wallet.Currency currency) {
        Account account = new Account();
        AccountPK accountPK = new AccountPK();
        accountPK.setCurrency(currency);
        accountPK.setUserId(userId);
        account.accountPK = accountPK;
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void decrease(double value) {
        this.amount -= value;
    }

    public void increase(double value) {
        this.amount += value;
    }

    public Set <Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set <Transaction> transactions) {
        this.transactions = transactions;
    }

    public AccountPK getAccountPK() {
        return accountPK;
    }

    public void setAccountPK(AccountPK accountPK) {
        this.accountPK = accountPK;
    }
}

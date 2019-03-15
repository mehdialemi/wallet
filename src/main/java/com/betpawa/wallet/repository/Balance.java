package com.betpawa.wallet.repository;

import com.betpawa.wallet.commons.Currency;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "balance")
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id", unique = true, nullable = false)
    private Long balanceId;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Currency currency;

    @OneToMany(mappedBy = "balance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet <>();
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "account_id", nullable = false)
//    private Account account;

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Set <Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set <Transaction> transactions) {
        this.transactions = transactions;
    }
//
//    public Account getAccount() {
//        return account;
//    }
//
//    public void setAccount(Account account) {
//        this.account = account;
//    }
}

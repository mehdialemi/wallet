package com.betpawa.wallet.repository;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private Long id;

    @Column(unique = true)
    private String userId;

    @OneToMany
    private Set<Balance> balance;

    @OneToMany
    private Set<Transaction> transactions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set <Balance> getBalance() {
        return balance;
    }

    public void setBalance(Set <Balance> balance) {
        this.balance = balance;
    }

    public Set <Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set <Transaction> transactions) {
        this.transactions = transactions;
    }
}

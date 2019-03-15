package com.betpawa.wallet.repository;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", unique = true, nullable = false)
    private Long accountId;

    @Column(unique = true)
    @NaturalId
    private String userId;

//    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Balance> balance = new HashSet <>();

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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
}

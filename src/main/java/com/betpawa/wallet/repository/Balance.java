package com.betpawa.wallet.repository;

import com.betpawa.wallet.commons.Currency;

import javax.persistence.*;

@Entity
@Table(name = "balances")
public class Balance {

    @Id
    private Long id;

    @Column
    private Long amount;

    @Enumerated
    @Column(nullable = false, columnDefinition = "smallint")
    private Currency currency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}

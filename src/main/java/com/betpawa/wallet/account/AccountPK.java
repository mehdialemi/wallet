package com.betpawa.wallet.account;

import com.betpawa.wallet.commons.Currency;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class AccountPK implements Serializable {

    @Column(name = "user_id")
    protected String userId;

    @Enumerated
    @Column(name = "currency")
    protected Currency currency;

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return super.equals(obj);

        AccountPK pk = (AccountPK) obj;
        return userId == pk.userId && currency == pk.currency;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}

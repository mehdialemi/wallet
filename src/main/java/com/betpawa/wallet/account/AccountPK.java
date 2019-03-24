package com.betpawa.wallet.account;

import com.betpawa.wallet.proto.Wallet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class AccountPK implements Serializable {

    @Column(name = "user_id", length = 20)
    protected String userId;

    @Enumerated
    @Column(name = "currency", columnDefinition = "smallint")
    protected Wallet.Currency currency;

    static AccountPK createNew(String userId, Wallet.Currency currency) {
        AccountPK accountPK = new AccountPK();
        accountPK.userId = userId;
        accountPK.currency = currency;
        return accountPK;
    }

    public void setCurrency(Wallet.Currency currency) {
        this.currency = currency;
    }

    public Wallet.Currency getCurrency() {
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
            return false;

        AccountPK pk = (AccountPK) obj;
        return userId.equals(pk.userId) && currency == pk.currency;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}

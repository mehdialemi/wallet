package com.betpawa.wallet.account;

import com.betpawa.wallet.proto.Wallet;
import org.hibernate.Session;

import java.util.List;

@SuppressWarnings("JpaQueryApiInspection")
class AccountRepository {

    boolean accountExists(Session session, String userId) {
        return findByUserId(session, userId).size() != 0;
    }

    List <Account> findByUserId(Session session, String userId) {
        return session.createNamedQuery("find_by_user_id", Account.class)
                .setParameter("userId", userId)
                .list();
    }

    Account findByUserIdCurrency(Session session, String userId, Wallet.Currency currency) {
        return session.get(Account.class, AccountPK.createNew(userId, currency));
    }
}

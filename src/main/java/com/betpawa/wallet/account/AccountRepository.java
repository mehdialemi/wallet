package com.betpawa.wallet.account;

import com.betpawa.wallet.commons.Currency;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.util.List;

public class AccountRepository {

    public static boolean accountExists(Session session, String userId) {
        return  findByUserId(session, userId).size() != 0;
    }

    public static List <Account> findByUserId(Session session, String userId) {
        Query <Account> query = session.createNamedQuery("find_by_user_id", Account.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public static Account findByUserIdCurrency(Session session, String userId, Currency currency) {
        Query <Account> query = session.createNamedQuery("find_by_user_id_currency", Account.class);
        query.setParameter("userId", userId);
        query.setParameter("currency", currency.ordinal());
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

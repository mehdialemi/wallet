package com.betpawa.wallet.services;

import com.betpawa.wallet.commons.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.FlushModeType;
import java.io.Closeable;

/**
 * Get a new session by {@code HibernateUtil.getSessionFactory}.
 * Manage transactions by starting transaction at {@link #openSession()} and committing transaction
 * at {@link #close()}
 */
public class MySession implements Closeable {

    private Session session;

    private MySession() {}

    /**
     * Open a new session and start a transaction
     * Note that closing the session should be done by the {#link {@link #close()}} function
     * @return a new created session with begin transaction state
     */
    public Session openSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        return session;
    }

    /**
     * Regarding current session, commit transaction and close it
     */
    @Override
    public void close() {
        if (session != null && session.isOpen()) {
            Transaction transaction = session.getTransaction();
            if (transaction.getStatus().isOneOf(TransactionStatus.ACTIVE))
                transaction.commit();
            else
                transaction.rollback();

            session.close();
        }
    }

    /**
     * This is the only function for instantiating this class
     * @return a new instance of class
     */
    public static MySession newSession() {
        return new MySession();
    }
}

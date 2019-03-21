package com.betpawa.wallet.commons;

import org.hibernate.Session;

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
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        return session;
    }

    /**
     * Regarding current session, commit transaction and close it
     */
    @Override
    public void close() {
        if (session != null && session.isOpen()) {
            session.getTransaction().commit();
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

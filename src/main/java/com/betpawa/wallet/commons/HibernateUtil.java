package com.betpawa.wallet.commons;

import com.betpawa.wallet.entities.Account;
import com.betpawa.wallet.entities.Balance;
import com.betpawa.wallet.entities.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory ;
    private static StandardServiceRegistry registry;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration config = new Configuration().configure();
                config.addAnnotatedClass(Account.class);
                config.addAnnotatedClass(Balance.class);
                config.addAnnotatedClass(Transaction.class);
                config.getProperties().put(Environment.SHOW_SQL, false);
                config.getProperties().put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");

                logger.info("Hibernate config is loaded successfully");

                registry = new StandardServiceRegistryBuilder()
                        .applySettings(config.getProperties()).build();

                Metadata metadata = new MetadataSources(registry)
                        .addAnnotatedClass(Account.class)
                        .addAnnotatedClass(Balance.class)
                        .addAnnotatedClass(Transaction.class)
                        .getMetadataBuilder()
                        .build();

                sessionFactory = metadata.getSessionFactoryBuilder().build();

                logger.info("SessionFactory is created by StandardServiceRegistry");

            } catch (Exception e) {
                e.printStackTrace();
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }

        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}

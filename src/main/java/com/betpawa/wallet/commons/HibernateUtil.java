package com.betpawa.wallet.commons;

import com.betpawa.wallet.account.Account;
import com.betpawa.wallet.account.AccountPK;
import com.betpawa.wallet.account.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.ehcache.internal.EhcacheRegionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory ;
    private static StandardServiceRegistry registry;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration config = new Configuration().configure();
                Properties properties = config.getProperties();
                properties.put(Environment.SHOW_SQL, false);
                properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");

                properties.put(Environment.USE_SECOND_LEVEL_CACHE, true);
                properties.put(Environment.CACHE_REGION_FACTORY, EhcacheRegionFactory.class);
                properties.put("hibernate.cache.ehcache.missing_cache_strategy", "create");

                registry = new StandardServiceRegistryBuilder()
                        .applySettings(properties).build();

                Metadata metadata = new MetadataSources(registry)
                        .addAnnotatedClass(Account.class)
                        .addAnnotatedClass(AccountPK.class)
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

package com.danyarko.reportingresolution.hibernate;

import com.danyarko.reportingresolution.configuration.DatasourceProperty;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static HibernateUtil hibernateUtil;
    private static Configuration configuration;
    private static SessionFactory sessionFactory;

    private HibernateUtil(DatasourceProperty datasourceProperty) {
        try {
            String configFile = "hibernate.cfg.xml";
            configuration = new Configuration();
            configuration.configure(configFile);

            configuration.setProperty("hibernate.connection.driver_class", datasourceProperty.getDriverClassName());
            configuration.setProperty("hibernate.dialect", datasourceProperty.getDialect());
            configuration.setProperty("hibernate.connection.url", datasourceProperty.getUrl());
            configuration.setProperty("hibernate.connection.username", datasourceProperty.getUsername());
            configuration.setProperty("hibernate.connection.password", datasourceProperty.getPassword());

            configuration.setProperty("show_sql", datasourceProperty.getShowSql());
            sessionFactory = configuration.buildSessionFactory();
        } catch (SecurityException | HibernateException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private static void getInstance(DatasourceProperty datasourceProperty) {
        if (hibernateUtil == null) {
            hibernateUtil = new HibernateUtil(datasourceProperty);
        }
    }

    private static void reconnect() throws HibernateException {
        sessionFactory = configuration.buildSessionFactory();
    }

    private static Session getSession() {
        Session session = sessionFactory.openSession();
        // Session session = sessionFactory.getCurrentSession();
        if (!session.isConnected()) {
            reconnect();
        }
        session.beginTransaction();
        return session;
    }

    public static Session beginTransaction(DatasourceProperty datasourceProperty) {
        getInstance(datasourceProperty);
        return getSession();
    }

    public static void commitTransaction(Session session) {
        try {
            if (session != null && session.isConnected()) {
                session.getTransaction().commit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void rollBackTransaction(Session session) {
        try {
            if (session != null && session.isConnected()) {
                session.getTransaction().rollback();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void closeTransaction(Session session) {
        if (session != null && session.isConnected()) {
            session.close();
        }
    }

    public static void closeSessionFactory() {
        if (sessionFactory != null && sessionFactory.isOpen()) {
            try {
                sessionFactory.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

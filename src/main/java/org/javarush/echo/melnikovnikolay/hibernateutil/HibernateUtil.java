package org.javarush.echo.melnikovnikolay.hibernateutil;

import org.hibernate.SessionFactory;

import java.util.Properties;

public class HibernateUtil {
    protected static Properties properties;

    protected SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

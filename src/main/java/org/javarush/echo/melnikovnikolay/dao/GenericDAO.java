package org.javarush.echo.melnikovnikolay.dao;

import org.hibernate.SessionFactory;

public abstract class GenericDAO {
    protected final SessionFactory sessionFactory;

    public GenericDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

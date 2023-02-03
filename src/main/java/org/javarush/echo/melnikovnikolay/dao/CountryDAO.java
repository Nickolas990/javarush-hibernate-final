package org.javarush.echo.melnikovnikolay.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.javarush.echo.melnikovnikolay.domain.Country;

import java.util.List;

public class CountryDAO extends GenericDAO{

    public CountryDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Country> getAll() {
        Query<Country> query = sessionFactory.getCurrentSession().createQuery("select c from Country c join fetch c.languages", Country.class);
        return query.list();
    }
}

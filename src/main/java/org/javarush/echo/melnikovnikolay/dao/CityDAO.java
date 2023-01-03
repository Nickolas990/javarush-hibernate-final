package org.javarush.echo.melnikovnikolay.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.javarush.echo.melnikovnikolay.domain.City;

import java.util.List;

public class CityDAO extends GenericDAO {

    public CityDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<City> getItems(int offset, int limit) {
        Query<City> query = sessionFactory.getCurrentSession().createQuery("select c from City c", City.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }
    public int getTotalCount() {
        Query<Long> query = sessionFactory.getCurrentSession().createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}

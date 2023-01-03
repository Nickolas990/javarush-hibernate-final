package org.javarush.echo.melnikovnikolay.hibernateutil;


import org.hibernate.cfg.Configuration;
import org.javarush.echo.melnikovnikolay.domain.City;
import org.javarush.echo.melnikovnikolay.domain.Country;
import org.javarush.echo.melnikovnikolay.domain.CountryLanguage;

import java.io.IOException;
import java.util.Properties;

public class HibernateMySQLUtil extends HibernateUtil {
    public HibernateMySQLUtil() {
        initialiseProps();
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .buildSessionFactory();
    }

    private static void initialiseProps() {
        try {
            properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("hibernateMySQL.properties"));
            properties.setProperty("hibernate.connection.username", System.getenv("MYSQL_DB_USER"));
            properties.setProperty("hibernate.connection.password", System.getenv("MYSQL_DB_PASSWORD"));
            properties.setProperty("hibernate.connection.url", System.getenv("DB_URL"));
        } catch (IOException e) {
            throw new RuntimeException("Properties-file for Hibernate (MySQL) not found");
        }
    }
}

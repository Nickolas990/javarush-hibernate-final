package org.javarush.echo.melnikovnikolay;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.javarush.echo.melnikovnikolay.dao.CityDAO;
import org.javarush.echo.melnikovnikolay.dao.CountryDAO;
import org.javarush.echo.melnikovnikolay.domain.City;
import org.javarush.echo.melnikovnikolay.domain.Country;
import org.javarush.echo.melnikovnikolay.domain.CountryLanguage;
import org.javarush.echo.melnikovnikolay.hibernateutil.HibernateMySQLUtil;
import org.javarush.echo.melnikovnikolay.hibernateutil.HibernateUtil;
import org.javarush.redis.CityCountry;
import org.javarush.redis.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


public class App {
    private final HibernateUtil hibernateMySQLUtil = new HibernateMySQLUtil();
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;
    private final ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;

    public App() {
        this.redisClient = prepareRedisClient();
        this.sessionFactory = hibernateMySQLUtil.getSessionFactory();
        this.mapper = new ObjectMapper();
        this.cityDAO = new CityDAO(sessionFactory);
        this.countryDAO = new CountryDAO(sessionFactory);
    }

    public static void main(String[] args) {
        App app = new App();
        List<City> allCities = app.fetchData(app);
        List<CityCountry> preparedData = app.transformData(allCities);
        app.shutdown();
    }

    private RedisClient prepareRedisClient() {
        return null;
    }

    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<City> fetchData(App app) {
        try (Session session = app.sessionFactory.getCurrentSession()) {
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();
            List<Country> countries = app.countryDAO.getAll();

            int totalCount = app.cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(app.cityDAO.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }


    private List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            Country country = city.getCountry();
            Set<CountryLanguage> countryLanguages = country.getLanguages();

            Set<Language> languages = countryLanguages.stream().map(cl -> Language.builder()
                    .language(cl.getLanguage())
                    .isOfficial(cl.getIsOfficial())
                    .percentage(cl.getPercentage())
                    .build()).collect(Collectors.toSet());

            return CityCountry.builder()
                    .id(city.getId())
                    .name(city.getName())
                    .population(city.getPopulation())
                    .district(city.getDistrict())
                    .alternativeCountryCode(country.getAlternativeCode())
                    .continent(country.getContinent())
                    .countryCode(country.getCode())
                    .countryName(country.getName())
                    .countryPopulation(country.getPopulation())
                    .countryRegion(country.getRegion())
                    .countrySurfaceArea(country.getSurfaceArea())
                    .languages(languages)
                    .build();
        }).collect(Collectors.toList());
    }
}

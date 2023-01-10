package org.javarush.echo.melnikovnikolay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
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
        app.pushToRedis(preparedData);
        app.testRedisData(List.of(1, 2, 3, 4));

        app.sessionFactory.getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        app.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        app.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        app.shutdown();
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }

        return redisClient;
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> synchronisation = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    synchronisation.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void testRedisData(List<Integer> ids) {
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id: ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void testMysqlData(List<Integer> ids) {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
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

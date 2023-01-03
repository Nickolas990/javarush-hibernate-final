FROM maven:latest as maven_builder
RUN mkdir -p /app
WORKDIR /app
ADD pom.xml /app
RUN mvn -B dependency:resolve dependency:resolve-plugins
ADD src /app/src
RUN mvn package

FROM mysql:8-debian
MAINTAINER darenrilie

ENV MYSQL_ROOT_PASSWORD root
ENV MYSQL_USER darenrilie
ENV MYSQL_PASSWORD qwerty

ADD dump-hibernate-final.sql /docker-entrypoint-initdb.d
RUN -v mysql:/var/lib/mysql

EXPOSE 3306

FROM redis
COPY redis.conf /usr/local/etc/redis/redis.conf
CMD [ "redis-server", "/usr/local/etc/redis/redis.conf" ]
EXPOSE 6379
EXPOSE 8001

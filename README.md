# aiml-service

## Install software

- Spring Boot - 2.0.4.RELEASE
- JDK - 1.8
- Spring Framework - 5.0.8 RELEASE
- Spring Data JPA
- Maven - 3.2+
- Oracle - 18+

## Configuration

Configuration for the application is at `application.properties`. The following parameters can be set in config files or in env variables:

- spring.jpa.hibernate.ddl-auto: 
- spring.datasource.url: oracle jdbc url
- spring.datasource.username: db user name
- spring.datasource.password: db user password
- spring.datasource.driver-class: jdbc driver class
- spring.datasource.hikari.connection-timeout: db connection timeout
- spring.datasource.hikari.maximum-pool-size: db connection pool maximum

## Deployment

When working locally, you will run the following commands (after setting the necessary environment variables):

- mvn spring-boot:run

Note that the oracle user and tables have been set up correctly. The database initialization script is in `sqls/Member-Compliance.sql`

## Docker Deployment

- starting oracle db `docker-compose up -d db`
- after the db container's status is healthy, init db `./docker-db/init-db.sh`
- package the jar `mvn clean package`
- starting aiml service `docker-compose up -d aiml-service`

## Insert KMP test Data

```
$ mvn clean package -DskipTests
$ mvn exec:java@kmp-test-data
```

## Run the checkstyle

For checking

```bash
mvn com.coveo:fmt-maven-plugin:check
```

For formatting

```bash
mvn com.coveo:fmt-maven-plugin:format
```

## Validation

Using postman collections in `docs` to test the system.

## Test data

Run mvn exec:java for functionality test. 

Run ```mvn exec:java -Dexec.mainClass="scripts.DataGenerator"``` for fake data generation.

Run ```mvn exec:java -Dexec.mainClass="scripts.DataGenerator" -Dexec.args="insertMTRFile"``` for MTR validation test. 


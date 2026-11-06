package com.example.educationalqualityproject.reservation;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;

@Configuration
@EnableTransactionManagement
public class ReservationPersistenceConfiguration {

    @Bean
    public Clock reservationClock() {
        return Clock.systemUTC();
    }

    @Bean(name = "reservationDataSource")
    @ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
    public DataSource reservationDataSource(
            @Value("${reservations.datasource.url}") String url,
            @Value("${reservations.datasource.username:}") String username,
            @Value("${reservations.datasource.password:}") String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "reservationJdbcTemplate")
    @ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
    public JdbcTemplate reservationJdbcTemplate(@Qualifier("reservationDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "reservationTransactionManager")
    @ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
    public PlatformTransactionManager reservationTransactionManager(
            @Qualifier("reservationDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
    public Flyway reservationFlyway(@Qualifier("reservationDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }
}
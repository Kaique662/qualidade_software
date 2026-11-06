package com.example.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackageClasses = PerformanceApplication.class)
public class PerformanceApplication {

    @Bean(destroyMethod = "close")
    HikariDataSource performanceDataSource(
            @Value("${perf.db-url:jdbc:postgresql://127.0.0.1:5432/perf_reservations}") String jdbcUrl,
            @Value("${perf.db-username:perfuser}") String username,
            @Value("${perf.db-password}") String password) {
        PerformanceReservationStore.requireLoopbackUrl(jdbcUrl);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(5000);
        config.setPoolName("local-performance-postgres");
        return new HikariDataSource(config);
    }

    @Bean
    ApplicationRunner initializePerformanceDatabase(PerformanceReservationStore store) {
        return args -> store.initialize();
    }

    public static void main(String[] args) {
        SpringApplication.run(PerformanceApplication.class, args);
    }
}

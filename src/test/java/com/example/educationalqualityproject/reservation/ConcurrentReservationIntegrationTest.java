package com.example.educationalqualityproject.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class ConcurrentReservationIntegrationTest {

    private static final Instant START = Instant.parse("2026-03-01T09:00:00Z");
    private static final Instant END = Instant.parse("2026-03-01T10:00:00Z");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservation_test")
            .withUsername("testuser")
            .withPassword("testpass");

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS btree_gist");
        jdbcTemplate.execute("DROP TABLE IF EXISTS room_reservation");
        jdbcTemplate.execute("DROP TABLE IF EXISTS teacher_agenda");

        jdbcTemplate.execute("CREATE TABLE room_reservation (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "resource_id VARCHAR(64) NOT NULL, " +
                "time_range TSTZRANGE NOT NULL, " +
                "status VARCHAR(16) NOT NULL DEFAULT 'ACEITA', " +
                "EXCLUDE USING gist (resource_id WITH =, time_range WITH &&)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE teacher_agenda (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "professor_id VARCHAR(64) NOT NULL, " +
                "time_range TSTZRANGE NOT NULL, " +
                "status VARCHAR(16) NOT NULL DEFAULT 'ACEITA', " +
                "EXCLUDE USING gist (professor_id WITH =, time_range WITH &&)" +
                ")");
    }

    @RepeatedTest(5)
    void roomReservation_sameSlot_concurrentRequests_shouldAcceptExactlyOneAndRejectNineteenWithConflict() throws Exception {
        assertConcurrentInsertOutcome(
                "room_reservation",
                "resource_id",
                "room-101",
                "ROOM"
        );
    }

    @RepeatedTest(5)
    void professorAgenda_sameSlot_concurrentRequests_shouldAcceptExactlyOneAndRejectNineteenWithConflict() throws Exception {
        assertConcurrentInsertOutcome(
                "teacher_agenda",
                "professor_id",
                "prof-42",
                "PROFESSOR"
        );
    }

    private void assertConcurrentInsertOutcome(String tableName, String keyColumn, String keyValue, String scenario) throws Exception {
        final int concurrentRequests = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    jdbcTemplate.update(
                            "INSERT INTO " + tableName + " (" + keyColumn + ", time_range, status) VALUES (?, tstzrange(?, ?, '[)'), ?)",
                            keyValue,
                            START,
                            END,
                            "ACEITA"
                    );
                    accepted.incrementAndGet();
                    return 201;
                } catch (Exception ex) {
                    if (isExclusionConflict(ex)) {
                        rejected.incrementAndGet();
                        return 409;
                    }
                    throw new IllegalStateException("Unexpected DB error while inserting " + scenario, ex);
                }
            }));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS), "Não todas as threads ficaram prontas");
        start.countDown();

        int acceptedTotal = 0;
        int rejectedTotal = 0;
        for (Future<Integer> future : futures) {
            int status = future.get(15, TimeUnit.SECONDS);
            if (status == 201) {
                acceptedTotal++;
            } else if (status == 409) {
                rejectedTotal++;
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, acceptedTotal, "Caso " + scenario + ": esperava 1 aceite");
        assertEquals(19, rejectedTotal, "Caso " + scenario + ": esperava 19 recusadas");
        assertEquals(1, accepted.get(), "Caso " + scenario + ": contagem de aceites não bate");
        assertEquals(19, rejected.get(), "Caso " + scenario + ": contagem de recusas não bate");

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + keyColumn + " = ? AND lower(time_range) = ? AND upper(time_range) = ?",
                Integer.class,
                keyValue,
                START,
                END
        );
        assertEquals(1, rows, "Caso " + scenario + ": deveria existir uma única linha para o recurso/intervalo");
    }

    private boolean isExclusionConflict(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException && "23P01".equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

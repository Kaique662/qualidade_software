package com.example.educationalqualityproject.reservation;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

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
        private DriverManagerDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        jdbcTemplate = new JdbcTemplate(dataSource);
        Flyway.configure().dataSource(dataSource).load().migrate();
        jdbcTemplate.execute("TRUNCATE TABLE reservation_audit_event, reservation, room_reservation, " +
            "teacher_agenda, material_reservation RESTART IDENTITY CASCADE");
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

    @Test
    void materialReservations_sameIntervalConcurrentRequests_shouldNotExceedCapacity() throws Exception {
        int concurrentRequests = 2;
        int capacity = 5;
        int requestedQuantity = 3;
        ReservationJdbcRepository repository = new ReservationJdbcRepository(jdbcTemplate);
        TransactionTemplate transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int index = 0; index < concurrentRequests; index++) {
            int requesterNumber = index;
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                ReservationRequest request = new ReservationRequest("projector-1",
                        ReservationRequest.ResourceType.MATERIAL, "user-" + requesterNumber,
                        null, START, END, requestedQuantity);
                try {
                    transactions.execute(status -> repository.create(request, false, requestedQuantity,
                            START, END, capacity, List.of()));
                    accepted.incrementAndGet();
                } catch (ResponseStatusException exception) {
                    if (exception.getStatusCode().value() != 409) {
                        throw exception;
                    }
                    rejected.incrementAndGet();
                }
                return null;
            }));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS), "Não todas as threads ficaram prontas");
        start.countDown();
        for (Future<?> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, accepted.get());
        assertEquals(1, rejected.get());
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM material_reservation WHERE resource_id = ? AND status = 'SOLICITADA'",
                Integer.class, "projector-1");
        assertEquals(1, rows);
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
                            "APROVADA"
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

package com.example.performance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Repository
public class PerformanceReservationStore implements PerformanceReservationRepository {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1");
    private final DataSource dataSource;

    public PerformanceReservationStore(DataSource dataSource,
            @Value("${perf.db-url:jdbc:postgresql://127.0.0.1:5432/perf_reservations}") String jdbcUrl) {
        requireLoopbackUrl(jdbcUrl);
        this.dataSource = dataSource;
    }

    public static void requireLoopbackUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("Performance database must use a local PostgreSQL JDBC URL");
        }
        String host = URI.create(jdbcUrl.substring("jdbc:".length())).getHost();
        if (host == null || !LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Performance database must be bound to loopback");
        }
    }

    public void initialize() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE EXTENSION IF NOT EXISTS btree_gist");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS room_reservation (
                            id BIGSERIAL PRIMARY KEY,
                            resource_id VARCHAR(64) NOT NULL,
                            time_range TSTZRANGE NOT NULL,
                            status VARCHAR(16) NOT NULL DEFAULT 'ACEITA',
                            EXCLUDE USING gist (resource_id WITH =, time_range WITH &&)
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS teacher_agenda (
                            id BIGSERIAL PRIMARY KEY,
                            professor_id VARCHAR(64) NOT NULL,
                            time_range TSTZRANGE NOT NULL,
                            status VARCHAR(16) NOT NULL DEFAULT 'ACEITA',
                            EXCLUDE USING gist (professor_id WITH =, time_range WITH &&)
                        )
                        """);
                statement.execute("CREATE INDEX IF NOT EXISTS idx_room_reservation_resource "
                        + "ON room_reservation (resource_id)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_teacher_agenda_professor "
                        + "ON teacher_agenda (professor_id)");
            }
        }
    }

    @Override
    public boolean isAvailable(String resourceId, Instant startsAt, Instant endsAt) throws SQLException {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT NOT EXISTS (
                         SELECT 1
                         FROM room_reservation
                         WHERE resource_id = ?
                           AND time_range && tstzrange(?::timestamptz, ?::timestamptz, '[)')
                     )
                     """)) {
            statement.setString(1, resourceId);
            statement.setString(2, startsAt.toString());
            statement.setString(3, endsAt.toString());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    @Override
    public boolean reserve(String resourceId, String teacherId, Instant startsAt, Instant endsAt)
            throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertRoom(connection, resourceId, startsAt, endsAt);
                insertTeacher(connection, teacherId, startsAt, endsAt);
                connection.commit();
                return true;
            } catch (SQLException exception) {
                rollback(connection, exception);
                if ("23P01".equals(exception.getSQLState())) {
                    return false;
                }
                throw exception;
            }
        }
    }

    private void insertRoom(Connection connection, String resourceId, Instant startsAt, Instant endsAt)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO room_reservation (resource_id, time_range, status)
                VALUES (?, tstzrange(?::timestamptz, ?::timestamptz, '[)'), 'ACEITA')
                """)) {
            statement.setString(1, resourceId);
            statement.setString(2, startsAt.toString());
            statement.setString(3, endsAt.toString());
            statement.executeUpdate();
        }
    }

    private void insertTeacher(Connection connection, String teacherId, Instant startsAt, Instant endsAt)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO teacher_agenda (professor_id, time_range, status)
                VALUES (?, tstzrange(?::timestamptz, ?::timestamptz, '[)'), 'ACEITA')
                """)) {
            statement.setString(1, teacherId);
            statement.setString(2, startsAt.toString());
            statement.setString(3, endsAt.toString());
            statement.executeUpdate();
        }
    }

    private static void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }
}

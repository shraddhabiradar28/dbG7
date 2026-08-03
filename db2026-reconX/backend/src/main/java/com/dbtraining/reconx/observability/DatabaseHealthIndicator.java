package com.dbtraining.reconx.observability;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;

@Component("reconxDatabase")
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        super("ReconX database health check failed");
        this.dataSource = dataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        long start = System.nanoTime();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout((int) TIMEOUT.toSeconds());
            try (ResultSet rs = stmt.executeQuery("SELECT 1")) {
                rs.next();
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            builder.up()
                    .withDetail("query", "SELECT 1")
                    .withDetail("elapsedMs", elapsedMs);
        } catch (SQLException e) {
            builder.down(e).withDetail("query", "SELECT 1");
        }
    }
}
package com.abc.inc.auth;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class RdbClient {
    private final static String DB_SCHEMA = "food-delivery";

    private static HikariDataSource dataSource;

    static {
        String url = "jdbc:mysql://"+System.getenv("RDB_HOST")+":"+System.getenv("RDB_PORT")+"/"+DB_SCHEMA;
        // Initialize the connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url); // e.g., "jdbc:mysql://your-rds-endpoint:3306/your-database"
        config.setUsername(System.getenv("RDB_USERNAME")); // e.g., "admin"
        config.setPassword(System.getenv("RDB_PASSWORD")); // e.g., "password"
        config.setMaximumPoolSize(5); // Adjust based on your Lambda's concurrency
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000); // 30 seconds
        config.setMaxLifetime(600000); // 10 minutes
        config.setConnectionTimeout(10000); // 10 seconds

        dataSource = new HikariDataSource(config);
    }

    /**
     * Get a database connection from the connection pool.
     *
     * @return A Connection object.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool.
     */
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

package com.gvozd.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class Db {

    private static final Logger LOG = LoggerFactory.getLogger(Db.class);

    private static final HikariDataSource DATA_SOURCE = create();

    private Db() {}

    private static HikariDataSource create() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(require("DB_URL"));
        cfg.setUsername(require("DB_USER"));
        cfg.setPassword(require("DB_PASSWORD"));
        cfg.setMaximumPoolSize(5);

        HikariDataSource dataSource = new HikariDataSource(cfg);
        LOG.info("Database connection pool created, max size {}", cfg.getMaximumPoolSize());
        return dataSource;
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задана переменная окружения " + name);
        }
        return value;
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }
}
package com.gvozd.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class Db {
    private static final HikariDataSource DATA_SOURCE = create();

    private Db() {}

    private static HikariDataSource create() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(require("DB_URL"));
        cfg.setUsername(require("DB_USER"));
        cfg.setPassword(require("DB_PASSWORD"));
        cfg.setMaximumPoolSize(5);
        return new HikariDataSource(cfg);
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
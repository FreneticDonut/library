package com.gvozd.library.dao.impl;

import com.gvozd.library.dao.UserDao;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private final DataSource ds;

    public JdbcUserDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT id, email, password_hash, name, role
                FROM users
                WHERE email = ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя по email", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = """
                SELECT id, email, password_hash, name, role
                FROM users
                WHERE id = ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения пользователя", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки email", e);
        }
    }

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users (email, password_hash, name, role)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getName());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong("id"));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя", e);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("name"),
                Role.valueOf(rs.getString("role")));
    }
}
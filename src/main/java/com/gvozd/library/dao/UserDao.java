package com.gvozd.library.dao;

import com.gvozd.library.model.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    Optional<User> findById(long id);

    boolean existsByEmail(String email);

    User save(User user);
}
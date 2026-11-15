package com.gvozd.library.model;

import java.io.Serializable;

public class SessionUser implements Serializable {

    private final long id;
    private final String name;
    private final Role role;

    public SessionUser(long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static SessionUser from(User user) {
        return new SessionUser(user.getId(), user.getName(), user.getRole());
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
}
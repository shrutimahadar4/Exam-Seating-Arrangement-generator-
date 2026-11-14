package com.examseating.model;

/**
 * Admin model class extending User
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String username, String password, String name) {
        super(id, username, password, name, "ADMIN");
    }
}

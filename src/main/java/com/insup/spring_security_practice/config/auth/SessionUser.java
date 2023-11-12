package com.insup.spring_security_practice.config.auth;

import com.insup.spring_security_practice.user.User;

public class SessionUser {

    private String name;
    private String email;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }

}

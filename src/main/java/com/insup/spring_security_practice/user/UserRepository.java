package com.insup.spring_security_practice.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<com.insup.spring_security_practice.user.User, Long> {

    Optional<com.insup.spring_security_practice.user.User> findByEmail(String email);

}

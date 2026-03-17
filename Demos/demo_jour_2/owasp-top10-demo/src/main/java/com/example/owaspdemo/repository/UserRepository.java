package com.example.owaspdemo.repository;

import com.example.owaspdemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Requete parametree securisee (JPQL).
     * Le parametre :username est automatiquement echappe par JPA.
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password")
    Optional<User> findByCredentials(@Param("username") String username, @Param("password") String password);

    /**
     * Recherche securisee par email.
     */
    List<User> findByEmailContainingIgnoreCase(String email);
}

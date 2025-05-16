package com.amigo.secreto.repositories;

import com.amigo.secreto.models.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(@Email String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT COUNT(g) FROM Group g JOIN g.participants p WHERE p.id = :userId")
    int countParticipatingGroups(@Param("userId") UUID userId);

    @Query("SELECT COUNT(g) FROM Group g JOIN g.participants p WHERE p.id = :userId AND g.draw IS NOT NULL")
    int countParticipatingDraws(@Param("userId") UUID userId);
}
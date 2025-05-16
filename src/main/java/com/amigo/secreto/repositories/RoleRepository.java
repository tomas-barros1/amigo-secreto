package com.amigo.secreto.repositories;

import com.amigo.secreto.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByName(String name);

  boolean existsByName(String roleName);
}
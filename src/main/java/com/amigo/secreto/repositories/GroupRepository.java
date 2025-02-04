package com.amigo.secreto.repositories;

import com.amigo.secreto.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

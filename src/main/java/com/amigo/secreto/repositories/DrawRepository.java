package com.amigo.secreto.repositories;

import com.amigo.secreto.models.Draw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrawRepository extends JpaRepository<Draw, UUID> {
}
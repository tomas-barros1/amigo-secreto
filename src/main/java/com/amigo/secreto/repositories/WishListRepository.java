package com.amigo.secreto.repositories;

import com.amigo.secreto.models.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishListRepository extends JpaRepository<WishList, UUID> {
}
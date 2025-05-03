package com.pos.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.cart.entity.CartMenuEntity;

@Repository
public interface CartMenuJpaRepository extends JpaRepository<CartMenuEntity, Long> {
}

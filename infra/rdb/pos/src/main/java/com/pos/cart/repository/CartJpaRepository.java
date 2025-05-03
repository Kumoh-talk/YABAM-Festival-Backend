package com.pos.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.cart.entity.CartEntity;
import com.pos.cart.repository.dsl.CartDslRepository;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, Long>, CartDslRepository {
}

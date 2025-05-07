package com.pos.order.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.repository.querydsl.OrderMenuQueryDslRepository;

public interface OrderMenuJpaRepository extends JpaRepository<OrderMenuEntity, Long>, OrderMenuQueryDslRepository {
}

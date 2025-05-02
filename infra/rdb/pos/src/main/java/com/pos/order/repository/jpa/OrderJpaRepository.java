package com.pos.order.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.order.entity.OrderEntity;
import com.pos.order.repository.querydsl.OrderQueryDslRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long>, OrderQueryDslRepository {

}

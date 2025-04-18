package com.pos.sale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.sale.entity.SaleEntity;

@Repository
public interface SaleJpaRepository extends JpaRepository<SaleEntity, Long> {
}

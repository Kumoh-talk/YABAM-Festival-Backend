package com.pos.receipt.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.receipt.entity.ReceiptCustomerEntity;

public interface ReceiptCustomerJpaRepository extends JpaRepository<ReceiptCustomerEntity, Long> {

}

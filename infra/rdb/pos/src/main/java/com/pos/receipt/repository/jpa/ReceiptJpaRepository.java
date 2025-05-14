package com.pos.receipt.repository.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.receipt.repository.querydsl.ReceiptQueryDslRepository;

public interface ReceiptJpaRepository extends JpaRepository<ReceiptEntity, UUID>, ReceiptQueryDslRepository {

}

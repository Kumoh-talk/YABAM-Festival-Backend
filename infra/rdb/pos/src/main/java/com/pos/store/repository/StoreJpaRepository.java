package com.pos.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.store.entity.StoreEntity;

@Repository
public interface StoreJpaRepository extends JpaRepository<StoreEntity, Long> {
}

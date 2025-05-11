package com.pos.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.store.entity.StoreEntity;
import com.pos.store.repository.dsl.StoreDslRepository;

@Repository
public interface StoreJpaRepository extends JpaRepository<StoreEntity, Long>, StoreDslRepository {
}

package com.pos.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.store.entity.StoreDetailImageEntity;

@Repository
public interface StoreDetailImageJpaRepository extends JpaRepository<StoreDetailImageEntity, Long> {
}

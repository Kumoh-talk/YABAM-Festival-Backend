package com.pos.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.store.entity.StoreDetailImageEntity;

@Repository
public interface DetailImageJpaRepository extends JpaRepository<StoreDetailImageEntity, Long> {
	List<StoreDetailImageEntity> findByStoreId(Long storeId);

	void deleteByImageUrl(String imageUrl);
}

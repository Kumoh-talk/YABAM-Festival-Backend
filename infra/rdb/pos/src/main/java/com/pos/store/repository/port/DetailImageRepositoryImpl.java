package com.pos.store.repository.port;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.store.repository.DetailImageJpaRepository;

import domain.pos.store.entity.DetailImages;
import domain.pos.store.port.required.DetailImageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DetailImageRepositoryImpl implements DetailImageRepository {
	private final DetailImageJpaRepository detailImageJpaRepository;

	@Override
	public DetailImages findByStoreId(Long queryStoreId) {
		return DetailImages.of(queryStoreId,
			detailImageJpaRepository.findByStoreId(queryStoreId)
				.stream()
				.map(StoreDetailImageEntity::getImageUrl)
				.toList());
	}

	@Override
	@Transactional
	public void save(DetailImages detailImages) {
		// 기존에 있던 이미지 중에서 요청에 없는 이미지는 삭제
		detailImageJpaRepository.findByStoreId(detailImages.getStoreId())
			.stream()
			.filter(entity ->
				!detailImages.getImageUrls().contains(entity.getImageUrl()))
			.forEach(detailImageJpaRepository::delete);

		// 요청에 있는 이미지 중에서 기존에 없던 이미지는 추가
		detailImageJpaRepository.saveAll(
			detailImages.getImageUrls().stream()
				.map(url -> StoreMapper.toDetailImageEntity(url, detailImages.getStoreId()))
				.toList());
	}
}

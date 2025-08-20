package com.pos.menu.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.mapper.MenuCategoryMapper;
import com.pos.menu.repository.jpa.MenuCategoryJpaRepository;

import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.repository.MenuCategoryRepository;
import domain.pos.store.entity.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MenuCategoryRepositoryImpl implements MenuCategoryRepository {
	private final MenuCategoryJpaRepository menuCategoryJpaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private final Integer TEMPORARY_ORDER = -1;

	@Override
	public MenuCategory postMenuCategory(Store store, MenuCategoryInfo menuCategoryInfo) {
		MenuCategoryEntity menuCategoryEntity = MenuCategoryMapper.toMenuCategoryEntity(menuCategoryInfo, store);
		menuCategoryJpaRepository.findMaxOrderByStoreId(store.getId())
			.ifPresentOrElse(
				order -> {
					if (order > 99) {
						throw new ServiceException(ErrorCode.MENU_CATEGORY_QUANTITY_OVERFLOW);
					}
					menuCategoryEntity.updateOrder(order + 1);
				},
				() -> menuCategoryEntity.updateOrder(1)
			);

		menuCategoryJpaRepository.save(menuCategoryEntity);
		return MenuCategoryMapper.toMenuCategory(menuCategoryEntity, store);
	}

	@Override
	public Optional<MenuCategoryInfo> getMenuCategoryInfo(Long storeId, Long categoryId) {
		return menuCategoryJpaRepository.findByIdAndStoreId(categoryId, storeId)
			.map(MenuCategoryMapper::toMenuCategoryInfo);
	}

	@Override
	public List<MenuCategoryInfo> getAllByStoreIdWithLock(Long storeId) {
		return menuCategoryJpaRepository.findAllByStoreIdWithLock(storeId)
			.stream()
			.map(MenuCategoryMapper::toMenuCategoryInfo)
			.toList();
	}

	@Override
	public List<MenuCategoryInfo> getMenuCategoryInfoList(Long storeId) {
		return menuCategoryJpaRepository.findAllByStoreId(storeId)
			.stream()
			.map(MenuCategoryMapper::toMenuCategoryInfo)
			.toList();
	}

	@Override
	public boolean existsMenuCategory(Long storeId, Long menuCategoryId) {
		return menuCategoryJpaRepository.existsByIdAndStoreId(menuCategoryId, storeId);
	}

	@Override
	public boolean existsMenuCategoryOrder(Long storeId, int menuCategoryOrder) {
		return menuCategoryJpaRepository.existsMenuCategoryOrder(storeId, menuCategoryOrder);
	}

	@Override
	public MenuCategoryInfo patchMenuCategory(MenuCategoryInfo patchMenuCategoryInfo) {
		MenuCategoryEntity menuCategoryEntity = menuCategoryJpaRepository.findById(patchMenuCategoryInfo.getId()).get();
		menuCategoryEntity.updateWithoutOrder(patchMenuCategoryInfo);
		return MenuCategoryMapper.toMenuCategoryInfo(menuCategoryEntity);
	}

	@Override
	public MenuCategoryInfo patchMenuCategoryOrder(Long storeId, MenuCategoryInfo menuCategoryInfo,
		Integer patchOrder) {
		if (menuCategoryInfo.getOrder().equals(patchOrder)) {
			return menuCategoryInfo;
		}
		MenuCategoryEntity menuCategoryEntity = menuCategoryJpaRepository.findById(menuCategoryInfo.getId()).get();
		menuCategoryEntity.updateOrder(TEMPORARY_ORDER);
		entityManager.flush();

		if (menuCategoryInfo.getOrder().compareTo(patchOrder) < 0) {
			menuCategoryJpaRepository.decreaseOrderInRange(storeId, menuCategoryInfo.getOrder(), patchOrder);
		} else {
			menuCategoryJpaRepository.increaseOrderInRange(storeId, patchOrder, menuCategoryInfo.getOrder());
		}

		menuCategoryEntity.updateOrder(patchOrder);
		return MenuCategoryMapper.toMenuCategoryInfo(menuCategoryEntity);
	}

	@Override
	public void deleteMenuCategory(Long storeId, Long categoryId) {
		MenuCategoryEntity menuCategoryEntity = menuCategoryJpaRepository.findById(categoryId).get();
		Integer periodOrder = menuCategoryEntity.getOrder();
		menuCategoryEntity.updateOrder(null);
		entityManager.flush();

		menuCategoryJpaRepository.delete(menuCategoryEntity);
		menuCategoryJpaRepository.decreaseOrderWhereGT(storeId, periodOrder);
	}
}

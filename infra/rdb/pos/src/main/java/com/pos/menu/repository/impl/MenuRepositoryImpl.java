package com.pos.menu.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.menu.entity.MenuEntity;
import com.pos.menu.mapper.MenuCategoryMapper;
import com.pos.menu.mapper.MenuMapper;
import com.pos.menu.repository.jpa.MenuJpaRepository;

import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuCategory;
import domain.pos.menu.entity.MenuCategoryInfo;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.menu.repository.MenuRepository;
import domain.pos.store.entity.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {
	private final MenuJpaRepository menuJpaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private final Integer TEMPORARY_ORDER = -1;

	@Override
	@Transactional
	public Menu postMenu(Store store, MenuCategoryInfo menuCategoryInfo, MenuInfo menuInfo) {
		MenuEntity menuEntity = MenuMapper.toMenuEntity(menuInfo, store, menuCategoryInfo);
		menuJpaRepository.findMaxOrderByMenuCategoryId(menuCategoryInfo.getId())
			.ifPresentOrElse(
				order -> {
					if (order > 99) {
						throw new ServiceException(ErrorCode.MENU_QUANTITY_OVERFLOW);
					}
					menuEntity.updateOrder(order + 1);
				},
				() -> menuEntity.updateOrder(1)
			);
		menuJpaRepository.save(menuEntity);
		return MenuMapper.toMenu(menuEntity, store, MenuCategory.fromWithoutStore(menuCategoryInfo));
	}

	@Override
	public Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId) {
		return menuJpaRepository.findByIdAndStoreId(menuId, storeId)
			.map(MenuMapper::toMenuInfo);
	}

	@Override
	public Optional<MenuInfo> getMenuInfo(Long storeId, Long menuId, Long lastMenuCategoryId) {
		return menuJpaRepository.findByIdAndStoreIdAndMenuCategoryId(menuId, storeId, lastMenuCategoryId)
			.filter(menuEntity -> menuEntity.getMenuCategory().getId().equals(lastMenuCategoryId))
			.map(MenuMapper::toMenuInfo);
	}

	@Override
	public List<Menu> getAllByStoreIdWithCategoryAndLock(Long storeId) {
		return menuJpaRepository.findAllByStoreIdWithCategoryAndLock(storeId)
			.stream()
			.map(menuEntity -> MenuMapper.toMenu(menuEntity, null,
				MenuCategoryMapper.toMenuCategory(menuEntity.getMenuCategory(), null)))
			.collect(Collectors.toList());
	}

	@Override
	public Slice<Menu> getMenuSlice(int pageSize, Long storeId, MenuInfo lastMenuInfo,
		MenuCategoryInfo lastMenuCategoryInfo) {
		return menuJpaRepository.findSliceByMenuCategoryId(
				pageSize,
				storeId,
				lastMenuInfo == null ? null : lastMenuInfo.getOrder(),
				lastMenuCategoryInfo
			)
			.map(menuEntity -> MenuMapper.toMenu(menuEntity, null,
				MenuCategoryMapper.toMenuCategory(menuEntity.getMenuCategory(), null)));
	}

	@Override
	public List<MenuInfo> getCategoryMenuList(Long storeId, Long menuCategoryId) {
		return menuJpaRepository.findAllByStoreIdAndMenuCategoryId(storeId, menuCategoryId)
			.stream()
			.map(MenuMapper::toMenuInfo)
			.collect(Collectors.toList());
	}

	@Override
	public boolean existsMenu(Long storeId, Long menuId) {
		return menuJpaRepository.existsByIdAndStoreId(menuId, storeId);
	}

	@Override
	public boolean existsMenuOrder(Long menuCategoryId, Integer menuOrder) {
		return menuJpaRepository.existsMenuOrder(menuCategoryId, menuOrder);
	}

	@Override
	public MenuInfo patchMenuInfo(MenuInfo patchMenuInfo) {
		MenuEntity menuEntity = menuJpaRepository.findById(patchMenuInfo.getId()).get();
		menuEntity.updateWithoutOrder(patchMenuInfo);
		return MenuMapper.toMenuInfo(menuEntity);
	}

	@Override
	public MenuInfo patchMenuOrder(Menu menu, Integer patchOrder) {
		if (menu.getMenuInfo().getOrder().equals(patchOrder)) {
			return menu.getMenuInfo();
		}
		MenuEntity menuEntity = menuJpaRepository.findById(menu.getMenuInfo().getId()).get();
		menuEntity.updateOrder(TEMPORARY_ORDER);
		entityManager.flush();

		if (menu.getMenuInfo().getOrder().compareTo(patchOrder) < 0) {
			menuJpaRepository.decreaseOrderInRange(menu.getMenuCategory().getMenuCategoryInfo().getId(),
				menu.getMenuInfo().getOrder(),
				patchOrder);
		} else {
			menuJpaRepository.increaseOrderInRange(menu.getMenuCategory().getMenuCategoryInfo().getId(),
				patchOrder,
				menu.getMenuInfo().getOrder());
		}
		menuEntity.updateOrder(patchOrder);
		return MenuMapper.toMenuInfo(menuEntity);
	}

	@Override
	public void deleteMenu(Menu menu) {
		MenuEntity menuEntity = menuJpaRepository.findById(menu.getMenuInfo().getId()).get();
		Integer periodOrder = menuEntity.getOrder();
		menuEntity.updateOrder(null);
		entityManager.flush();

		menuJpaRepository.delete(menuEntity);
		menuJpaRepository.decreaseOrderWhereGT(menu.getMenuCategory().getMenuCategoryInfo().getId(),
			periodOrder);
	}

	@Override
	public Long countByIdIn(Long storeId, Set<Long> menuIds) {
		return menuJpaRepository.countByIdIn(storeId, menuIds);
	}

	@Override
	public Optional<MenuInfo> getMenuInfoById(Long menuId) {
		return menuJpaRepository.findById(menuId)
			.map(MenuMapper::toMenuInfo);
	}
}

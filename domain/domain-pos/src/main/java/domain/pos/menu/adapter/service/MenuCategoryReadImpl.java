package domain.pos.menu.adapter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import domain.pos.menu.entity.v2.domain.MenuCategory;
import domain.pos.menu.port.provided.MenuCategoryRead;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.store.implement.StoreValidator;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuCategoryReadImpl implements MenuCategoryRead {
	private final MenuCategoryRepository menuCategoryRepository;

	private final StoreValidator storeValidator;

	@Override
	public List<MenuCategory> readMenuCategoryList(Long storeId) {
		List<MenuCategory> menuCategoryList = menuCategoryRepository.readList(storeId);

		if (menuCategoryList.isEmpty()) {
			storeValidator.validateStore(storeId);
		}
		return menuCategoryList;
	}
}

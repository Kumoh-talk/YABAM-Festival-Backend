package domain.pos.menu.adapter.service;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.menu.entity.v2.domain.Menu;
import domain.pos.menu.entity.v2.dto.MenuSliceWithVersion;
import domain.pos.menu.port.provided.MenuRead;
import domain.pos.menu.port.required.MenuCategoryRepository;
import domain.pos.menu.port.required.MenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuReadImpl implements MenuRead {
	private final MenuRepository menuRepository;
	private final MenuCategoryRepository menuCategoryRepository;

	@Override
	public Menu readMenu(Long storeId, Long menuId) {
		return menuRepository.readMenu(storeId, menuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
	}

	@Override
	public MenuSliceWithVersion readMenuSlice(Long storeId, int pageSize, Long lastMenuId, Long version) {
		Menu lastMenu = lastMenuId != null
			? menuRepository.readMenu(storeId, lastMenuId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND))
			: null;
		// 스크롤 도중 메뉴 순서 변경이 이루어지면 페이지 새로고침 -> 낙관적 락을 통한 버전 확인
		validateVersion(lastMenu, version);

		Slice<Menu> menuSlice = menuRepository.readMenuSlice(pageSize, lastMenu);
		Long nextVersion = readNextVersion(menuSlice);
		return new MenuSliceWithVersion(menuSlice, nextVersion);
	}

	@Override
	public List<Menu> readCategoryMenuList(Long storeId, Long menuCategoryId) {
		List<Menu> menuList = menuRepository.readCategoryMenuList(menuCategoryId);
		if (!menuList.isEmpty()) {
			return menuList;
		}

		// 메뉴 리스트가 비었으면, 카테고리 존재 여부 확인
		if (!menuCategoryRepository.existsMenuCategory(storeId, menuCategoryId)) {
			throw new ServiceException(ErrorCode.MENU_CATEGORY_NOT_FOUND);
		} else {
			return menuList;
		}
	}

	private void validateVersion(Menu lastMenu, Long version) {
		// 버전 확인
		if (lastMenu != null) {
			Long lastVersion = menuCategoryRepository.readVersion(lastMenu.getMenuCategoryId());
			if (!lastVersion.equals(version)) {
				throw new ServiceException(ErrorCode.MENU_CATEGORY_VERSION_MISMATCH);
			}
		}
	}

	private Long readNextVersion(Slice<Menu> menuSlice) {
		if (menuSlice.getNumberOfElements() == 0) {
			return null;
		} else {
			return menuCategoryRepository.readVersion(
				menuSlice.getContent().get(menuSlice.getNumberOfElements() - 1).getMenuCategoryId());
		}
	}
}

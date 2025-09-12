package domain.pos.menu.entity.v2.dto;

import org.springframework.data.domain.Slice;

import domain.pos.menu.entity.v2.domain.Menu;

public class MenuSliceWithVersion {
	private final Slice<Menu> menuSlice;
	private final Long lastMenuVersion;

	public MenuSliceWithVersion(Slice<Menu> menuSlice, long lastMenuVersion) {
		this.menuSlice = menuSlice;
		this.lastMenuVersion = lastMenuVersion;
	}

	public Slice<Menu> getMenuSlice() {
		return menuSlice;
	}

	public long getLastMenuVersion() {
		return lastMenuVersion;
	}
}

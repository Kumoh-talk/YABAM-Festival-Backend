package domain.pos.store.entity;

import domain.pos.member.entity.UserPassport;
import lombok.Getter;

@Getter
public class Store {
	private final Long storeId;
	private final Boolean isOpen;
	private final StoreInfo storeInfo;
	private final UserPassport ownerPassport;

	private Store(Long storeId, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport) {
		this.storeId = storeId;
		this.isOpen = isOpen;
		this.storeInfo = storeInfo;
		this.ownerPassport = ownerPassport;
	}

	public static Store of(Long storeId, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport) {
		return new Store(storeId, isOpen, storeInfo, ownerPassport);
	}

	public Store open() {
		return new Store(this.storeId, true, this.storeInfo, this.ownerPassport);
	}

}

package domain.pos.store.entity;

import java.util.List;

import com.vo.UserPassport;

import lombok.Getter;

@Getter
public class Store {
	private final Long storeId;
	private final Boolean isOpen;
	private final StoreInfo storeInfo;
	private final UserPassport ownerPassport;
	private final List<String> detailImageUrls;

	private Store(Long storeId, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport,
		List<String> detailImageUrls) {
		this.storeId = storeId;
		this.isOpen = isOpen;
		this.storeInfo = storeInfo;
		this.ownerPassport = ownerPassport;
		this.detailImageUrls = detailImageUrls != null ? List.copyOf(detailImageUrls) : List.of();
	}

	public static Store of(Long storeId, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport,
		List<String> detailImageUrls) {
		return new Store(storeId, isOpen, storeInfo, ownerPassport, detailImageUrls);
	}

	public Store open() {
		return new Store(this.storeId, true, this.storeInfo, this.ownerPassport, this.detailImageUrls);
	}

	public Store changeIsOpen() {
		return new Store(this.storeId, !this.isOpen, this.storeInfo, this.ownerPassport, this.detailImageUrls);
	}

}

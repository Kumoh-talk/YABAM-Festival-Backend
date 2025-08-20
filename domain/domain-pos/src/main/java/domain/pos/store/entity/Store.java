package domain.pos.store.entity;

import static com.exception.ErrorCode.*;
import static com.exception.State.*;
import static com.vo.UserRole.*;
import static java.util.Objects.*;

import java.util.List;

import com.vo.UserPassport;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class Store {
	private Long id;
	private Boolean isOpen;
	private StoreInfo storeInfo;
	private UserPassport ownerPassport;
	private List<String> detailImageUrls;

	private Store(Long id, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport,
		List<String> detailImageUrls) {
		this.id = id;
		this.isOpen = isOpen;
		this.storeInfo = storeInfo;
		this.ownerPassport = ownerPassport;
		this.detailImageUrls = detailImageUrls != null ? List.copyOf(detailImageUrls) : List.of();
	}

	private Store() {
	}

	public static Store of(Long storeId, Boolean isOpen, StoreInfo storeInfo, UserPassport ownerPassport,
		List<String> detailImageUrls) {
		return new Store(storeId, isOpen, storeInfo, ownerPassport, detailImageUrls);
	}

	public static Store create(UserPassport ownerPassport, StoreInfo requestStoreInfo) {
		state(isOwner(ownerPassport), NOT_OWNER_STORE_CREATE);

		var store = new Store();
		store.ownerPassport = requireNonNull(ownerPassport);
		store.storeInfo = requireNonNull(requestStoreInfo);

		return store;
	}

	private static boolean isOwner(UserPassport ownerPassport) {
		return ownerPassport.getUserRole().equals(ROLE_OWNER);
	}

	public Store open() {
		return new Store(this.id, true, this.storeInfo, this.ownerPassport, this.detailImageUrls);
	}

	public Store changeIsOpen() {
		return new Store(this.id, !this.isOpen, this.storeInfo, this.ownerPassport, this.detailImageUrls);
	}

	public void update(UserPassport ownerPassport, StoreInfo modifyStoreInfo) {
		state(isOwnerStore(ownerPassport), NOT_EQUAL_STORE_OWNER);

		this.storeInfo = requireNonNull(modifyStoreInfo);
	}

	private boolean isOwnerStore(UserPassport ownerPassport) {
		return ownerPassport.getUserId().equals(this.ownerPassport.getUserId());
	}
}

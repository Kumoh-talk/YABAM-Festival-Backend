package domain.pos.receipt.implement;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import domain.pos.receipt.entity.Receipt;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptValidator {
	public void validateAccessToReceipt(Receipt receipt, UserPassport userPassport) {
		if (receipt.getUserPassport() == null) {
			return;
		}
		Long customerId = receipt.getUserPassport().getUserId();
		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();

		if (!isCustomer(customerId, userPassport)
			&& !isStoreOwner(storeOwnerId, userPassport)) {
			log.warn("Receipt 접근 가능 요청자가 아닙니다. userId: {}", userPassport.getUserId());
			throw new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED);
		}
	}

	private boolean isCustomer(Long customerId, UserPassport userPassport) {
		return customerId.equals(userPassport.getUserId()) && userPassport.getUserRole() == UserRole.ROLE_USER;
	}

	private boolean isStoreOwner(Long storeOwnerId, UserPassport userPassport) {
		return storeOwnerId.equals(userPassport.getUserId()) && userPassport.getUserRole() == UserRole.ROLE_OWNER;
	}
}

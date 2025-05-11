package domain.pos.receipt.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.vo.UserPassport;
import com.vo.UserRole;

import domain.pos.receipt.entity.Receipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptValidator {
	private final ReceiptReader receiptReader;

	public void validateReceipt(Long receiptId) {
		if (!receiptReader.existsReceipt(receiptId)) {
			log.warn("영수증을 찾을 수 없습니다. receiptId: {}", receiptId);
			throw new ServiceException(ErrorCode.RECEIPT_NOT_FOUND);
		}
	}

	public UserRole validateRole(Receipt receipt, UserPassport userPassport) {
		if (isStoreOwner(receipt, userPassport)) {
			return UserRole.ROLE_OWNER;
		} else {
			return UserRole.ROLE_ANONYMOUS;
		}
	}

	public void validateIsOwner(Receipt receipt, UserPassport userPassport) {
		if (!isStoreOwner(receipt, userPassport)) {
			log.warn("요청자가 점주가 아닙니다. userId: {}", userPassport.getUserId());
			throw new ServiceException(ErrorCode.RECEIPT_ACCESS_DENIED);
		}
	}

	public boolean isStoreOwner(Receipt receipt, UserPassport userPassport) {
		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();
		return storeOwnerId.equals(userPassport.getUserId()) && userPassport.getUserRole() == UserRole.ROLE_OWNER;
	}
}

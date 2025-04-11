package domain.pos.receipt.implement;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import domain.pos.receipt.entity.Receipt;

public class ReceiptValidator {
	public boolean hasAccessToReceipt(Receipt receipt, UserPassport userPassport) {
		Long customerId = receipt.getUserPassport().getUserId();
		Long storeOwnerId = receipt.getSale().getStore().getOwnerPassport().getUserId();
		Long userId = userPassport.getUserId();

		return (customerId.equals(userId) && userPassport.getUserRole().equals(UserRole.ROLE_USER))
			||
			(storeOwnerId.equals(userId) && userPassport.getUserRole().equals(UserRole.ROLE_OWNER));
	}
}

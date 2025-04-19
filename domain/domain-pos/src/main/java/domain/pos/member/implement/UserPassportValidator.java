package domain.pos.member.implement;

import org.springframework.stereotype.Component;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;

@Component
public class UserPassportValidator {

	public void validateUserPassport(UserPassport userPassport, Long userId) {
		if (!userPassport.getUserId().equals(userId)) {
			throw new ServiceException(ErrorCode.USER_ACCESS_DENIED);
		}
	}
}

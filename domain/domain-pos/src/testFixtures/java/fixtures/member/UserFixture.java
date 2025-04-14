package fixtures.member;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;

public class UserFixture {
	// 유저 고유 id
	private static final Long GENERAL_USER_ID = 1L;
	private static final Long DIFF_USER_ID = 999L;
	// 유저 닉네임
	private static final String GENERAL_USER_NICKNAME = "유저1";
	// 유저 권한
	private static final UserRole OWNER_USER_ROLE = UserRole.ROLE_OWNER;
	private static final UserRole GENERAL_USER_ROLE = UserRole.ROLE_USER;

	public static UserPassport OWNER_USER_PASSPORT() {
		return UserPassport.of(
			GENERAL_USER_ID,
			GENERAL_USER_NICKNAME,
			OWNER_USER_ROLE);
	}

	public static UserPassport GENERAL_USER_PASSPORT() {
		return UserPassport.of(
			GENERAL_USER_ID,
			GENERAL_USER_NICKNAME,
			GENERAL_USER_ROLE);
	}

	public static UserPassport DIFF_OWNER_PASSPORT() {
		return UserPassport.of(
			DIFF_USER_ID,
			GENERAL_USER_NICKNAME,
			OWNER_USER_ROLE);
	}

	public static UserPassport CUSTOM_USER_PASSPORT(
		final Long userId,
		final String nickname,
		final UserRole userRole) {
		return UserPassport.of(
			userId,
			nickname,
			userRole);
	}
}

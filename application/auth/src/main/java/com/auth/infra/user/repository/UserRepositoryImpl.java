package com.auth.infra.user.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.auth.domain.repository.UserRepository;
import com.auth.domain.service.FakeUserService;
import com.auth.domain.vo.OidcProvider;
import com.auth.infra.user.entity.UserEntity;
import com.vo.UserPassport;
import com.vo.UserRole;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
	private final UserJpaRepository userJpaRepository;

	@Override
	public UserPassport findByEmailAndProviderAndProviderId(String email, OidcProvider provider, String providerId) {
		return userJpaRepository.findByEmailAndProviderAndProviderId(email, provider, providerId)
			.map(userEntity -> UserPassport.of(userEntity.getId(), userEntity.getNickname(), userEntity.getRole()))
			.orElse(null);
	}

	@Override
	@Transactional
	public UserPassport createUser(String email, OidcProvider provider, String providerId) {
		return createAndSave(email, provider, providerId, UserRole.ROLE_USER,
			FakeUserService.fakeUserEmail, "fakeUser");
	}

	@Override
	@Transactional
	public UserPassport createOwner(String email, OidcProvider provider, String providerId) {
		return createAndSave(email, provider, providerId, UserRole.ROLE_OWNER,
			FakeUserService.fakeOwnerEmail, "fakeOwner");
	}

	private UserPassport createAndSave(String email, OidcProvider provider, String providerId,
		UserRole role, String fakeEmail, String fakeNicknamePrefix) {
		UserEntity userEntity = UserEntity.builder()
			.email(email)
			.role(role)
			.provider(provider)
			.providerId(providerId)
			.build();
		UserEntity savedUserEntity = userJpaRepository.save(userEntity);
		String nickname = email.equals(fakeEmail)
			? fakeNicknamePrefix + savedUserEntity.getId()
			: "금붕이" + savedUserEntity.getId();
		savedUserEntity.setNickname(nickname);
		return UserPassport.of(savedUserEntity.getId(), savedUserEntity.getNickname(), savedUserEntity.getRole());
	}

	@Override
	public UserPassport getUserInfo(Long userId) {
		UserEntity userEntity = userJpaRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return UserPassport.of(userEntity.getId(), userEntity.getNickname(), userEntity.getRole());
	}
}

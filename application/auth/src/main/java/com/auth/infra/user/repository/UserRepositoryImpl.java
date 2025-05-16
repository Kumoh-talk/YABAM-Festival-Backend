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
		UserEntity userEntity = UserEntity.builder()
			.email(email)
			.role(UserRole.ROLE_USER)
			.provider(provider)
			.providerId(providerId)
			.build();

		UserEntity savedUserEntity = userJpaRepository.save(userEntity);
		if (email.equals(FakeUserService.fakeUserEmail)) {
			savedUserEntity.setNickname("fakeUser" + savedUserEntity.getId());
		} else {
			savedUserEntity.setNickname("금붕이" + savedUserEntity.getId());
		}
		return UserPassport.of(savedUserEntity.getId(), savedUserEntity.getNickname(), savedUserEntity.getRole());
	}

	@Override
	@Transactional
	public UserPassport createOwner(String email, OidcProvider provider, String providerId) {
		UserEntity userEntity = UserEntity.builder()
			.email(email)
			.role(UserRole.ROLE_OWNER)
			.provider(provider)
			.providerId(providerId)
			.build();

		UserEntity savedUserEntity = userJpaRepository.save(userEntity);
		if (email.equals(FakeUserService.fakeOwnerEmail)) {
			savedUserEntity.setNickname("fakeOwner" + savedUserEntity.getId());
		} else {
			savedUserEntity.setNickname("금붕이" + savedUserEntity.getId());
		}
		return UserPassport.of(savedUserEntity.getId(), savedUserEntity.getNickname(), savedUserEntity.getRole());
	}

	@Override
	public UserPassport getUserInfo(Long userId) {
		UserEntity userEntity = userJpaRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return UserPassport.of(userEntity.getId(), userEntity.getNickname(), userEntity.getRole());
	}
}

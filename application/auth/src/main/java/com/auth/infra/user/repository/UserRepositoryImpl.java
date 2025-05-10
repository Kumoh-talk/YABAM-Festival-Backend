package com.auth.infra.user.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.auth.domain.repository.UserRepository;
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
			.map(userEntity -> UserPassport.of(userEntity.getId(), userEntity.getEmail(), userEntity.getRole()))
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
		return UserPassport.of(savedUserEntity.getId(), savedUserEntity.getEmail(), savedUserEntity.getRole());
	}

	@Override
	public UserPassport createOwner(String email, OidcProvider provider, String providerId) {
		UserEntity userEntity = UserEntity.builder()
			.email(email)
			.role(UserRole.ROLE_OWNER)
			.provider(provider)
			.providerId(providerId)
			.build();

		UserEntity savedUserEntity = userJpaRepository.save(userEntity);
		return UserPassport.of(savedUserEntity.getId(), savedUserEntity.getEmail(), savedUserEntity.getRole());
	}
}

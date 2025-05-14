package com.auth.infra.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.domain.vo.OidcProvider;
import com.auth.infra.user.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmailAndProviderAndProviderId(String email, OidcProvider provider, String providerId);
}

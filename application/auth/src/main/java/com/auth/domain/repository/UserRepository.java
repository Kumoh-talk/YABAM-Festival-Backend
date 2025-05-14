package com.auth.domain.repository;

import org.springframework.stereotype.Repository;

import com.auth.domain.vo.OidcProvider;
import com.vo.UserPassport;

@Repository
public interface UserRepository {
	UserPassport findByEmailAndProviderAndProviderId(String email, OidcProvider provider, String providerId);

	UserPassport createUser(String email, OidcProvider provider, String providerId);

	UserPassport createOwner(String email, OidcProvider provider, String providerId);

	UserPassport getUserInfo(Long userId);

}

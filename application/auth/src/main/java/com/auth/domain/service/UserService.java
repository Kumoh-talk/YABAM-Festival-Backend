package com.auth.domain.service;

import org.springframework.stereotype.Service;

import com.auth.domain.implement.UserHandler;
import com.auth.domain.vo.OidcProvider;
import com.vo.UserPassport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserHandler userHandler;

	public UserPassport findOrCreateUser(String providerId, String email, OidcProvider provider) {
		UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(email, provider, providerId);

		return userPassport != null ? userPassport : userHandler.createUser(providerId, email, provider);
	}

	public UserPassport getUserInfo(Long userId) {
		return userHandler.getUserInfo(userId);
	}
}

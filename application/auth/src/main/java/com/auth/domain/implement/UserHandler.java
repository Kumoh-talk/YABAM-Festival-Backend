package com.auth.domain.implement;

import org.springframework.stereotype.Component;

import com.auth.domain.repository.UserRepository;
import com.auth.domain.vo.OidcProvider;
import com.vo.UserPassport;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserHandler {
	private final UserRepository userRepository;

	public UserPassport findByEmailAndProviderAndProviderId(String email, OidcProvider provider, String providerId) {
		return userRepository.findByEmailAndProviderAndProviderId(email, provider, providerId);
	}

	public UserPassport getUserInfo(Long userId) {
		return userRepository.getUserInfo(userId);
	}

	public UserPassport createUser(String providerId, String email, OidcProvider provider) {
		return userRepository.createUser(email, provider, providerId);
	}

	public UserPassport createOwner(String providerId, String email, OidcProvider provider) {
		return userRepository.createOwner(email, provider, providerId);
	}
}

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

	public UserPassport findByEmailAndProviderAndProviderId(String providerId, String email, OidcProvider provider) {
		return userRepository.findByEmailAndProviderAndProviderId(email, provider, providerId);
	}

	public UserPassport createUser(String providerId, String email, OidcProvider provider) {
		return userRepository.createUser(email, provider, providerId);
	}

	public UserPassport createOwner(String providerId, String email, OidcProvider provider) {
		return userRepository.createOwner(email, provider, providerId);
	}
}

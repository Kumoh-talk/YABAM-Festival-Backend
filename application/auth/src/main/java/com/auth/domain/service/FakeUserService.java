package com.auth.domain.service;

import org.springframework.stereotype.Service;

import com.auth.domain.implement.UserHandler;
import com.auth.domain.vo.OidcProvider;
import com.vo.UserPassport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FakeUserService {
	private volatile UserPassport fakeUserPassport = null;
	private volatile UserPassport fakeOwnerPassport = null;

	private final String fakeUserEmail = "fake-user-email";
	private final String fakeOwnerEmail = "fake-owner-email";

	private final OidcProvider fakeProvider = OidcProvider.KAKAO;

	private final String fakeProviderId = "fake-user-provider-id";

	private final UserHandler userHandler;

	public UserPassport fakeUserLogin() {
		if (fakeUserPassport == null) {
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeProviderId, fakeUserEmail,
				fakeProvider);

			if (userPassport != null) {
				fakeUserPassport = userPassport;
			} else {
				fakeUserPassport = userHandler.createUser(fakeProviderId, fakeUserEmail, fakeProvider);
			}
		}

		return fakeUserPassport;
	}

	public UserPassport fakeOwnerLogin() {
		if (fakeOwnerPassport == null) {
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeProviderId, fakeOwnerEmail,
				fakeProvider);

			if (userPassport != null) {
				fakeOwnerPassport = userPassport;
			} else {
				fakeOwnerPassport = userHandler.createOwner(fakeProviderId, fakeOwnerEmail, fakeProvider);
			}
		}

		return fakeOwnerPassport;
	}
}

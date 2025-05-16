package com.auth.domain.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.auth.domain.implement.UserHandler;
import com.auth.domain.vo.OidcProvider;
import com.vo.UserPassport;

import lombok.RequiredArgsConstructor;

@Profile({"local", "dev", "test"})
@Service
@RequiredArgsConstructor
public class FakeUserService {
	private volatile UserPassport fakeUserPassport = null;
	private volatile UserPassport fakeOwnerPassport = null;

	public static final String fakeUserEmail = "fake-user-email";
	public static final String fakeOwnerEmail = "fake-owner-email";

	private final OidcProvider fakeProvider = OidcProvider.KAKAO;

	private final String fakeProviderId = "fake-user-provider-id";

	private final UserHandler userHandler;

	public UserPassport fakeUserLogin() {
		if (fakeUserPassport == null) {
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeUserEmail, fakeProvider,
				fakeProviderId);

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
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeOwnerEmail, fakeProvider,
				fakeProviderId);

			if (userPassport != null) {
				fakeOwnerPassport = userPassport;
			} else {
				fakeOwnerPassport = userHandler.createOwner(fakeProviderId, fakeOwnerEmail, fakeProvider);
			}
		}

		return fakeOwnerPassport;
	}
}

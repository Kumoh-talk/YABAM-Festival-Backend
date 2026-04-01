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

	private static final OidcProvider FAKE_PROVIDER = OidcProvider.KAKAO;
	private static final String FAKE_PROVIDER_ID = "fake-user-provider-id";

	private final UserHandler userHandler;

	public UserPassport fakeUserLogin() {
		if (fakeUserPassport == null) {
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeUserEmail, FAKE_PROVIDER,
				FAKE_PROVIDER_ID);

			if (userPassport != null) {
				fakeUserPassport = userPassport;
			} else {
				fakeUserPassport = userHandler.createUser(FAKE_PROVIDER_ID, fakeUserEmail, FAKE_PROVIDER);
			}
		}

		return fakeUserPassport;
	}

	public UserPassport fakeOwnerLogin() {
		if (fakeOwnerPassport == null) {
			UserPassport userPassport = userHandler.findByEmailAndProviderAndProviderId(fakeOwnerEmail, FAKE_PROVIDER,
				FAKE_PROVIDER_ID);

			if (userPassport != null) {
				fakeOwnerPassport = userPassport;
			} else {
				fakeOwnerPassport = userHandler.createOwner(FAKE_PROVIDER_ID, fakeOwnerEmail, FAKE_PROVIDER);
			}
		}

		return fakeOwnerPassport;
	}
}

package com.auth.domain.implement;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.auth.domain.entity.OidcPayload;
import com.auth.domain.vo.OidcProvider;
import com.auth.infra.oidc.client.OidcClient;
import com.auth.infra.oidc.client.apple.AppleOidcClient;
import com.auth.infra.oidc.client.google.GoogleOidcClient;
import com.auth.infra.oidc.client.kakao.KakaoOidcClient;
import com.auth.infra.oidc.dto.OidcPublicKey;
import com.auth.infra.oidc.property.OidcClientProperties;
import com.auth.infra.oidc.property.apple.AppleOidcProperties;
import com.auth.infra.oidc.property.google.GoogleOidcProperties;
import com.auth.infra.oidc.property.kakao.KakaoOidcProperties;
import com.auth.infra.oidc.response.OidcPublicKeyResponse;
import com.exception.ErrorCode;
import com.exception.ServiceException;

@Component
public class OAuthOidcHelper {
	private final JwtOidcProvider jwtOidcProvider;
	private final Map<OidcProvider, Map<OidcClient, OidcClientProperties>> oauthOidcClients;

	public OAuthOidcHelper(
		JwtOidcProvider jwtOidcProvider,
		KakaoOidcClient kakaoOidcClient,
		GoogleOidcClient googleOidcClient,
		AppleOidcClient appleOidcClient,
		KakaoOidcProperties kakaoOidcProperties,
		GoogleOidcProperties googleOidcProperties,
		AppleOidcProperties appleOidcProperties
	) {
		this.jwtOidcProvider = jwtOidcProvider;
		this.oauthOidcClients = Map.of(
			OidcProvider.KAKAO, Map.of(kakaoOidcClient, kakaoOidcProperties),
			OidcProvider.GOOGLE, Map.of(googleOidcClient, googleOidcProperties),
			OidcProvider.APPLE, Map.of(appleOidcClient, appleOidcProperties)
		);
	}

	/**
	 * Provider에 따라 Client와 Properties를 선택하고 Odic public key 정보를 가져와서 ID Token의 payload를 추출하는 메서드
	 *
	 * @param provider : {@link OidcProvider}
	 * @param oauthId  : Provider에서 발급한 사용자 식별자
	 * @param idToken  : idToken
	 * @param nonce    : 인증 서버 로그인 요청 시 전달한 임의의 문자열
	 * @return OIDCDecodePayload : ID Token의 payload
	 */
	public OidcPayload getPayload(OidcProvider provider, String oauthId, String idToken, String nonce) {
		Map<OidcClient, OidcClientProperties> providerMap = oauthOidcClients.get(provider);
		OidcClient client = providerMap.keySet().iterator().next();
		OidcClientProperties properties = providerMap.values().iterator().next();
		OidcPublicKeyResponse response = client.getOidcPublicKey();
		String kid = jwtOidcProvider.getKidFromUnsignedTokenHeader(
			idToken, properties.getIssuer(), oauthId, properties.getSecrets(), nonce);
		OidcPublicKey key = response.getKeys().stream()
			.filter(k -> k.kid().equals(kid))
			.findFirst()
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_MATCHED_PUBLIC_KEY));
		return jwtOidcProvider.getOidcTokenBody(idToken, key.n(), key.e());
	}
}

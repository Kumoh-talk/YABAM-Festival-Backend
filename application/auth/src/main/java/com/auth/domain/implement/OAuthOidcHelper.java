package com.auth.domain.implement;

import java.util.List;
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
		OidcClient client = oauthOidcClients.get(provider).keySet().iterator().next();
		OidcClientProperties properties = oauthOidcClients.get(provider).values().iterator().next();
		OidcPublicKeyResponse response = client.getOidcPublicKey();
		return getPayloadFromIdToken(idToken, properties.getIssuer(), oauthId, properties.getSecrets(), nonce,
			response);
	}

	/**
	 * ID Token의 payload를 추출하는 메서드 <br/>
	 * OAuth 2.0 spec에 따라 ID Token의 유효성 검사 수행 <br/>
	 *
	 * @param idToken  : idToken
	 * @param iss      : ID Token을 발급한 provider의 URL
	 * @param sub      : ID Token의 subject (사용자 식별자)
	 * @param auds      : ID Token이 발급된 앱의 앱 키
	 * @param nonce    : 인증 서버 로그인 요청 시 전달한 임의의 문자열
	 * @param response : 공개키 목록
	 * @return OidcPayload : ID Token의 payload
	 */
	private OidcPayload getPayloadFromIdToken(String idToken, String iss, String sub, List<String> auds, String nonce,
		OidcPublicKeyResponse response) {
		String kid = jwtOidcProvider.getKidFromUnsignedTokenHeader(idToken, iss, sub, auds, nonce);
		OidcPublicKey key = response.getKeys().stream()
			.filter(k -> k.kid().equals(kid))
			.findFirst()
			.orElseThrow(() -> new ServiceException(ErrorCode.NOT_MATCHED_PUBLIC_KEY));
		return jwtOidcProvider.getOidcTokenBody(idToken, key.n(), key.e());
	}
}

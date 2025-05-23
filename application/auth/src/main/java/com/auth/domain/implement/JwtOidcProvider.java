package com.auth.domain.implement;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth.domain.entity.OidcPayload;
import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtOidcProvider {
	private static final String KID = "kid";
	private static final String RSA = "RSA";
	private final ObjectMapper objectMapper;

	/**
	 * ID Token의 header 에서 KID 값을 추출하는 메서드
	 */
	public String getKidFromUnsignedTokenHeader(String token, String iss, String sub, List<String> auds, String nonce) {
		return getUnsignedTokenClaims(token, iss, sub, auds, nonce).get("header").get(KID);
	}

	/**
	 * ID Token의 body를 추출하는 메서드
	 */
	public OidcPayload getOidcTokenBody(String token, String modulus, String exponent) {
		Claims body = getOidcTokenJws(token, modulus, exponent).getPayload();
		String aud = body.getAudience().iterator().next(); // aud가 여러개일 경우 첫 번째 aud를 사용

		return new OidcPayload(
			body.getIssuer(),
			aud,
			body.getSubject(),
			body.get("email", String.class));
	}

	/**
	 * ID Token의 header 와 payload 만 추출하는 메서드
	 */
	private String getUnsignedToken(String token) {
		String[] splitToken = token.split("\\.");
		if (splitToken.length != 3) {
			throw new ServiceException(ErrorCode.INVALID_ID_TOKEN);
		}
		return splitToken[0] + "." + splitToken[1] + ".";
	}

	/**
	 * ID Token의 header와 body를 Base64 방식으로 디코딩
	 * 페이로드의 iss 값이 소셜 링크와 일치하는지 확인
	 * 페이로드의 aud 값이 서비스 앱 키와 일치하는지 확인
	 * 페이로드의 nonce 값이 소셜 로그인 요청 시 전달한 값과 일치하는지 확인
	 */
	private Map<String, Map<String, String>> getUnsignedTokenClaims(String token, String iss, String sub,
		List<String> auds,
		String nonce) {
		try {
			Base64.Decoder decoder = Base64.getUrlDecoder();

			String unsignedToken = getUnsignedToken(token);
			String headerJson = new String(decoder.decode(unsignedToken.split("\\.")[0]));
			String payloadJson = new String(decoder.decode(unsignedToken.split("\\.")[1]));

			Map<String, String> header = objectMapper.readValue(headerJson, Map.class);
			Map<String, String> payload = objectMapper.readValue(payloadJson, Map.class);

			Assert.isTrue(payload.get("iss").equals(iss),
				"iss is not matched. expected : " + iss + ", actual : " + payload.get("iss"));
			Assert.isTrue(payload.get("sub").equals(sub),
				"sub is not matched. expected : " + sub + ", actual : " + payload.get("sub"));
			boolean matched = auds.stream().anyMatch(expectedAud -> expectedAud.equals(payload.get("aud")));
			Assert.isTrue(matched,
				"aud is not matched. expected one of : " + auds + ", actual : " + payload.get("aud"));

			return Map.of("header", header, "payload", payload);
		} catch (IllegalArgumentException e) {
			throw new ServiceException(ErrorCode.INVALID_ID_TOKEN);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * n과 e 의 조합으로 공개키를 생성하고 ID Token을 검증하는 메서드
	 */
	private Jws<Claims> getOidcTokenJws(String token, String modulus, String exponent) {
		try {
			return Jwts.parser()
				.verifyWith(getRsaPublicKey(modulus, exponent))
				.build()
				.parseSignedClaims(token);
		} catch (JwtException e) {
			throw new ServiceException(ErrorCode.INVALID_ID_TOKEN);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new ServiceException(ErrorCode.ABNORMAL_ID_TOKEN);
		}
	}

	/**
	 * 공개된 n, e 조합으로 공개키를 생성하는 메서드
	 */
	private PublicKey getRsaPublicKey(String modulus, String exponent) throws
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
		byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
		BigInteger nPublicKey = new BigInteger(1, decodeN);
		BigInteger ePublicKey = new BigInteger(1, decodeE);

		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(nPublicKey, ePublicKey);
		return keyFactory.generatePublic(publicKeySpec);
	}
}


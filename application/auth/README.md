# OIDC 구현 개발 가이드

## 1. OIDC 개요

OIDC(OpenID Connect)는 OAuth 2.0 프로토콜 기반의 인증 프로토콜이다. 카카오, 구글, 애플과 같은 제공자에서 사용자 인증을 처리하고, 이를 우리 서비스에서 활용할 수 있게 한다.
핵심 개념:

- **ID Token**: JWT 형식의 사용자 인증 정보 토큰
- **클레임(Claims)**: ID Token에 포함된 사용자 정보
- **Provider**: 인증 서비스 제공자 (카카오, 구글, 애플)

## 2. 구현 클래스 설명

### 2.1 OidcProvider (enum)

``` java
public enum OidcProvider {
    KAKAO,
    GOOGLE,
    APPLE;
}
```

- OIDC 인증 제공자를 표현하는 열거형

### 2.2 OidcPayload (record)

``` java
public record OidcPayload(
    /* issuer */
    String iss,
    /* client id */
    String aud,
    /* oauth provider account unique id */
    String sub,
    String email
) {}
```

- ID Token에서 추출한 정보를 담는 클래스
- `iss`: 발급자 URL
- `aud`: 클라이언트 ID
- `sub`: 사용자 식별자
- `email`: 사용자 이메일

### 2.3 JwtOidcProvider

JWT 형식의 ID Token을 처리하는 클래스
주요 기능:

- ID Token 헤더에서 KID(Key ID) 추출
- 공개키로 ID Token 검증
- ID Token의 페이로드 추출
- JWT 검증 및 파싱

### 2.4 OAuthOidcHelper

다양한 OIDC Provider를 처리하는 헬퍼 클래스
주요 기능:

- 각 Provider별 Client와 속성 관리
- Provider에 따른 Client 선택
- ID Token에서 페이로드 추출

## 3. 인증 흐름

OIDC 인증 흐름:

1. 클라이언트가 Provider에 로그인 요청
2. 사용자가 Provider에서 인증
3. Provider가 클라이언트에 ID Token 발급
4. 클라이언트가 ID Token을 서버에 전달
5. 서버가 ID Token 검증 후 사용자 인증 처리

## 4. API 개발 가이드

### 4.1 API 엔드포인트 설계

``` 
POST /api/v1/auth/login/oidc
```

### 4.2 요청 파라미터

``` json
{
  "provider": "KAKAO",
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAifQ...",
  "oauthId": "12345678",
  "nonce": "abc123"
}
```

### 4.3 구현 예시

컨트롤러 구현:

``` java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OidcAuthController {

    private final OAuthOidcHelper oAuthOidcHelper;
    private final UserService userService;

    @PostMapping("/login/oidc")
    public ResponseEntity<LoginResponse> loginWithOidc(@RequestBody OidcLoginRequest request) {
        // 1. ID Token에서 페이로드 추출
        OidcPayload payload = oAuthOidcHelper.getPayload(
            request.getProvider(), 
            request.getOauthId(), 
            request.getIdToken(), 
            request.getNonce()
        );
        
        // 2. 추출한 정보로 사용자 처리 (회원가입/로그인)
        User user = userService.findOrCreateUser(
            payload.sub(), 
            payload.email(), 
            request.getProvider()
        );
        
        // 3. 인증 토큰 발급
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);
        
        // 4. 응답 반환
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }
}
```

요청 DTO:

``` java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OidcLoginRequest {
    private OidcProvider provider;
    private String idToken;
    private String oauthId;
    private String nonce;
}
```

### 4.4 OAuthOidcHelper 사용 방법

실제 프로젝트 사용 예시:

``` java
private final JwtService jwtService;
private final OAuthOidcHelper oauthOidcHelper;
private final UserService userService;
private final NickNameGenerateService nickNameGenerateService;

@Transactional
public Pair<Long, JwtPair> signUp(OidcProvider provider, SignUpRequest.Oidc request) {
    // OAuthOidcHelper를 사용하여 ID Token에서 페이로드 추출
    OidcPayload payload = oauthOidcHelper.getPayload(
        provider,              // 인증 제공자 (KAKAO, GOOGLE, APPLE)
        request.oauthId(),     // 사용자 식별자
        request.idToken(),     // ID Token
        request.nonce()        // nonce 값
    );

    // 이메일로 기존 사용자 조회
    return userService.findByProviderAndEmail(OAuthMapper.fromOidcProvider(provider), payload.email())
        .map(user -> Pair.of(user.getId(), jwtService.createToken(user))) // 이메일이 존재할 경우
        .orElseGet(() -> { // 이메일이 존재하지 않을 경우
            // 새 사용자 생성
            User oAuthUser = UserMapper.toOAuthUser(
                nickNameGenerateService.generateRandomNickname(),
                OAuthMapper.fromOidcProvider(provider),
                payload.email()
            );
            User newUser = userService.registerOAuthUser(oAuthUser);
            return Pair.of(newUser.getId(), jwtService.createToken(newUser));
        });
}
```

ID Token에서 추출한 정보(특히 이메일)를 사용해 회원가입/로그인 로직을 구현한다.

## 5. 클라이언트 전달 데이터

클라이언트는 다음 데이터를 서버로 전송해야 한다:

1. **Provider**: 인증 제공자 (KAKAO, GOOGLE, APPLE)
2. **ID Token**: 제공자에서 받은 JWT 형식 토큰
3. **oauthId**: 제공자가 제공한 사용자 식별자
4. **nonce**: 인증 요청 시 사용한 임의 문자열 (선택)

각 제공자별 필요 정보:

- **카카오**: ID Token, 사용자 ID
- **구글**: ID Token, 사용자 ID
- **애플**: ID Token, 사용자 ID

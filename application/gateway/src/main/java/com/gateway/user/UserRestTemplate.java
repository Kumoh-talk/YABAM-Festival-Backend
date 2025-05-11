// package com.gateway.user;
//
// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestTemplate;
//
// import lombok.RequiredArgsConstructor;
//
// @Component
// @RequiredArgsConstructor
// public class UserRestTemplate {
// 	private final RestTemplate restTemplate;
//
// 	public boolean isAdmin(Long userId) {
// 		// try {
// 		// 	ResponseEntity<Role> response = restTemplate.exchange(
// 		// 		"http://user-service/v1/users/{userId}/role",
// 		// 		HttpMethod.GET,
// 		// 		null,
// 		// 		Role.class,
// 		// 		userId
// 		// 	);
// 		//
// 		// 	if (response.getStatusCode() == HttpStatus.OK) {
// 		// 		Role role = response.getBody();
// 		// 		if (role == Role.ROLE_ADMIN) {
// 		// 			return true;
// 		// 		} else {
// 		// 			return false;
// 		// 		}
// 		// 	} else {
// 		// 		// 에러 처리
// 		// 	}
// 		// } catch (HttpClientErrorException e) {
// 		// 	// 클라이언트 오류 처리 (예: 404 Not Found)
// 		// } catch (RestClientException e) {
// 		// 	// 기타 RestTemplate 예외 처리
// 		// }
// 		return true;
// 	}
// }

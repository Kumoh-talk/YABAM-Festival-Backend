package com.interceptor;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.config.InterceptorTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.http.HttpHeaderName;
import com.vo.UserPassport;
import com.vo.UserRole;

@SpringBootTest(classes = {
	InterceptorTestConfig.class
})
@EnableWebMvc
@AutoConfigureMockMvc
class InterceptorTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private final Long userId = 1004L;
	private final String userNickname = "tester";
	private final UserRole userRole = UserRole.ROLE_USER;
	private final UserRole ownerRole = UserRole.ROLE_OWNER;

	@Nested
	@DisplayName("HasRoleInterceptor 테스트")
	class DeserializingUserPassportInterceptorTest {
		@Test
		@DisplayName("헤더에 Json에 존재하면 이를 직렬화")
		void exists_header() throws Exception {
			// given : request 헤더에 Json 삽입
			Map<String, Object> userInfo = new LinkedHashMap<>();
			userInfo.put(UserPassport.getFieldUserId(), userId);
			userInfo.put(UserPassport.getFieldUserNickname(), userNickname);
			userInfo.put(UserPassport.getFieldUserRole(), userRole);
			String userInfoJson = objectMapper.writeValueAsString(userInfo);

			// when
			MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/userPassport")
					.header(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson))
				.andExpect(status().isOk())
				.andReturn();

			// then
			String responseBody = result.getResponse().getContentAsString();
			UserPassport response = objectMapper.readValue(responseBody, UserPassport.class);

			assertSoftly(softly -> {
				softly.assertThat(response.getUserId()).isEqualTo(userId);
				softly.assertThat(response.getUserNickname()).isEqualTo(userNickname);
				softly.assertThat(response.getUserRole()).isEqualTo(userRole);
			});
		}
	}

	@Nested
	@DisplayName("HasRoleInterceptor 테스트")
	class HasRoleInterceptorTest {
		@Test
		@DisplayName("owner 유저는 owner 권한 API 접근 할 수 있다.")
		void owner_access_owner_api() throws Exception {
			// given : request 헤더에 Json 삽입
			Map<String, Object> userInfo = new LinkedHashMap<>();
			userInfo.put(UserPassport.getFieldUserId(), userId);
			userInfo.put(UserPassport.getFieldUserNickname(), userNickname);
			userInfo.put(UserPassport.getFieldUserRole(), ownerRole);
			String userInfoJson = objectMapper.writeValueAsString(userInfo);

			// when
			MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/hasRole/owner")
					.header(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson))
				.andExpect(status().isOk())
				.andReturn();

			// then
			String responseBody = result.getResponse().getContentAsString();
			UserRole response = objectMapper.readValue(responseBody, UserRole.class);
			assertSoftly(softly -> {
				softly.assertThat(response).isEqualTo(ownerRole);
			});
		}

		@Test
		@DisplayName("owner 유저는 user 권한 API 접근 할 수 있다.")
		void owner_access_user_api() throws Exception {
			// given : request 헤더에 Json 삽입
			Map<String, Object> userInfo = new LinkedHashMap<>();
			userInfo.put(UserPassport.getFieldUserId(), userId);
			userInfo.put(UserPassport.getFieldUserNickname(), userNickname);
			userInfo.put(UserPassport.getFieldUserRole(), ownerRole);
			String userInfoJson = objectMapper.writeValueAsString(userInfo);

			// when
			MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/hasRole/user")
					.header(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson))
				.andExpect(status().isOk())
				.andReturn();

			// then
			String responseBody = result.getResponse().getContentAsString();
			UserRole response = objectMapper.readValue(responseBody, UserRole.class);
			assertSoftly(softly -> {
				softly.assertThat(response).isEqualTo(ownerRole);
			});
		}

		@Test
		@DisplayName("UserRole 지정 HasRole 테스트")
		void user_not_access_owner_api() throws Exception {
			// given : request 헤더에 Json 삽입
			Map<String, Object> userInfo = new LinkedHashMap<>();
			userInfo.put(UserPassport.getFieldUserId(), userId);
			userInfo.put(UserPassport.getFieldUserNickname(), userNickname);
			userInfo.put(UserPassport.getFieldUserRole(), userRole);
			String userInfoJson = objectMapper.writeValueAsString(userInfo);

			// when -> then
			mockMvc.perform(MockMvcRequestBuilders.get("/test/hasRole/owner")
					.header(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson))
				.andExpect(status().isForbidden());
		}
	}
}

package com.aop;

import static com.interceptor.DeserializingUserPassportInterceptor.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.AopTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.config.AopTestConfig;
import com.vo.UserPassport;
import com.vo.UserRole;

@SpringBootTest(classes = {
	AopTestConfig.class
})
@EnableAspectJAutoProxy
class AopTest {
	@Autowired
	private AopTestHelper aopTestHelper;

	@Test
	@DisplayName("AssignUserPassport AOP 테스트")
	void assignUserPassportTest() {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();
		UserPassport testUserPassport = new UserPassport(123L, "testuser", UserRole.ROLE_USER);
		request.setAttribute(USER_INFO_ATTRIBUTE, testUserPassport);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		// when
		UserPassport result = aopTestHelper.assignUserPassport(null);

		// then
		assertSoftly(softly -> {
			softly.assertThat(result.getUserId()).isEqualTo(testUserPassport.getUserId());
			softly.assertThat(result.getUserNickname()).isEqualTo(testUserPassport.getUserNickname());
			softly.assertThat(result.getUserRole()).isEqualTo(testUserPassport.getUserRole());
		});
	}

	@Test
	@DisplayName("DeadlockRetry AOP 테스트")
	void deadlockRetryTest() {
		// when
		int flag = aopTestHelper.deadlockRetry();

		// then
		assertSoftly(softly -> {
			softly.assertThat(flag).isZero();

			AopTestHelper target = AopTestUtils.getTargetObject(aopTestHelper);
			softly.assertThat(target.firstTransaction).isNotEqualTo(target.secondTransaction);
		});
	}
}

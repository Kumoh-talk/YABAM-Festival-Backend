package com.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gateway.component.JwtIssueFunction;
import com.gateway.filter.gateway.CommitCheckFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

	private final JwtIssueFunction jwtIssueFunction;

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("yabam", r -> r
				.order(0)
				.path("/yabam/**")
				.filters(f -> f
					.rewritePath("/yabam/(?<remaining>.*)", "/${remaining}")
				)
				.uri("lb://yabam")
			)

			.route("auth_login_route", r -> r
				.order(1)
				.path("/auth/api/login/**")
				.filters(f -> f
					.filter(new CommitCheckFilter())
					.rewritePath("/auth/(?<remaining>.*)", "/${remaining}")
					.modifyResponseBody(String.class, String.class, jwtIssueFunction)
				)
				.uri("lb://auth"))

			// .route("auth_default_route", r -> r
			// 	.order(2)
			// 	.path("/auth/**")
			// 	.and()
			// 	.predicate(exchange -> !exchange.getRequest().getPath().toString().startsWith("/auth/api/login"))
			// 	.filters(f -> f
			// 		.rewritePath("/auth/(?<remaining>.*)", "/${remaining}")
			// 	)
			// 	.uri("lb://auth")
			// )

			.build();
	}
}

package com.application.festival.yabam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authorization.AssignUserPassport;
import com.authorization.HasRole;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/yabam")
public class HelloController {

	@GetMapping("/hello")
	@HasRole(userRole = UserRole.ROLE_ANONYMOUS)
	@AssignUserPassport
	public ResponseEntity<Void> hello(UserPassport userPassport) {
		return ResponseEntity.ok().build();
	}
}

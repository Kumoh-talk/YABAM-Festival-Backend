package com.auth.infra.user.entity;

import com.auth.domain.vo.OidcProvider;
import com.vo.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "role", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private UserRole role;

	@Column(name = "provider", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private OidcProvider provider;

	@Column(name = "providerId", nullable = false)
	private String providerId;

	@Builder
	public UserEntity(String email, UserRole role, OidcProvider provider, String providerId) {
		this.email = email;
		this.role = role;
		this.provider = provider;
		this.providerId = providerId;
	}
}

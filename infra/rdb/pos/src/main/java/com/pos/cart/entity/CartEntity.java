package com.pos.cart.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pos.receipt.entity.ReceiptEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "carts", indexes = {
	@Index(name = "uk_carts_receipt_id", columnList = "receipt_id", unique = true)
})
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receipt_id")
	private ReceiptEntity receipt;

	@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY)
	private List<CartMenuEntity> cartMenus = new ArrayList<>();

	@Column(name = "session_token")
	private UUID sessionToken;

	@Column(name = "pending_at")
	private LocalDateTime pendingAt;

	private CartEntity(ReceiptEntity receipt) {
		this.receipt = receipt;
	}

	public static CartEntity from(ReceiptEntity receipt) {
		return new CartEntity(receipt);
	}

	public void startSession(UUID token) {
		this.sessionToken = token;
		this.pendingAt = LocalDateTime.now();
	}

	public void clearSession() {
		this.sessionToken = null;
		this.pendingAt = null;
	}
}

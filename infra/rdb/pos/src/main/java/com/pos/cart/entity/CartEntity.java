package com.pos.cart.entity;

import java.util.ArrayList;
import java.util.List;

import com.pos.receipt.entity.ReceiptEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "carts")
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

	private CartEntity(ReceiptEntity receipt) {
		this.receipt = receipt;
	}

	public static CartEntity from(ReceiptEntity receipt) {
		return new CartEntity(receipt);
	}
}

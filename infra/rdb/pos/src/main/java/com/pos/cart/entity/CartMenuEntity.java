package com.pos.cart.entity;

import com.pos.menu.entity.MenuEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "cart_menus")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartMenuEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "cart_id")
	private CartEntity cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "menu_id")
	private MenuEntity menu;

	@Column(nullable = false)
	private Integer quantity;

	private CartMenuEntity(CartEntity cart, MenuEntity menu, Integer quantity) {
		this.cart = cart;
		this.menu = menu;
		this.quantity = quantity;
	}

	public static CartMenuEntity of(CartEntity cart, MenuEntity menu, Integer quantity) {
		return new CartMenuEntity(cart, menu, quantity);
	}

	public void plusQuantity(Integer quantity) {
		this.quantity += quantity;
	}
}

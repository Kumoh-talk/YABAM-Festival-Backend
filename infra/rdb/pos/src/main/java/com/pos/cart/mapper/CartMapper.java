package com.pos.cart.mapper;

import java.util.List;
import java.util.UUID;

import com.pos.cart.entity.CartEntity;
import com.pos.menu.entity.MenuEntity;
import com.pos.menu.mapper.MenuMapper;

import domain.pos.cart.entity.Cart;
import domain.pos.cart.entity.CartMenu;
import domain.pos.menu.entity.MenuInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartMapper {
	public static Cart toCart(final UUID receiptId, final CartEntity cartEntity) {
		List<CartMenu> cartMenus = cartEntity.getCartMenus().stream()
			.map(cartMenuEntity -> {
				MenuEntity menu = cartMenuEntity.getMenu();
				MenuInfo menuInfo = MenuMapper.toMenuInfo(menu);
				return CartMenu.of(cartMenuEntity.getQuantity(), menuInfo);
			})
			.toList();
		return Cart.of(receiptId, cartMenus);
	}
}

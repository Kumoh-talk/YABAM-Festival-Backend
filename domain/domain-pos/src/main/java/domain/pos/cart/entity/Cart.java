package domain.pos.cart.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Cart {
	private UUID receiptId;
	private List<CartMenu> cartMenus;
	private UUID sessionToken;
	private LocalDateTime pendingAt;

	private Cart(UUID receiptId, List<CartMenu> cartMenus, UUID sessionToken, LocalDateTime pendingAt) {
		this.receiptId = receiptId;
		this.cartMenus = cartMenus;
		this.sessionToken = sessionToken;
		this.pendingAt = pendingAt;
	}

	public static Cart of(UUID receiptId, List<CartMenu> cartMenus) {
		return new Cart(receiptId, cartMenus, null, null);
	}

	public static Cart of(UUID receiptId, List<CartMenu> cartMenus, UUID sessionToken, LocalDateTime pendingAt) {
		return new Cart(receiptId, cartMenus, sessionToken, pendingAt);
	}

	public boolean isPending() {
		return sessionToken != null && pendingAt != null
			&& pendingAt.plusSeconds(60).isAfter(LocalDateTime.now());
	}

	public boolean isSessionOwner(UUID token) {
		return isPending() && sessionToken.equals(token);
	}
}

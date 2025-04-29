## SSE Event Server 의 역할

### OwnerChannel

#### StoreOrderEvent

##### 설명

- 주문이 생성되었거나 주문의 상태 변환시 발생하는 이벤트입니다.

```java
public record StoreOrderEvent(
	Long tableId,
	Integer tableNumber,
	Order order
) {
	public record Order(
		Long orderId,
		LocalDateTime createdAt,
		List<OrderMenu> orderMenu
	) {
		public record OrderMenu(
			Long menuId,
			String menuName,
			Integer menuPrice,
			Integer menuCount,
			String menuStatus // TODO : 추후 enum으로 변경
		) {
		}
	}
}
```

### CustomerChannel

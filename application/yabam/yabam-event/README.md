## SSE Event Server 의 역할

> SSE 서버에서 제공하는 준 실시간성 데이터에 대해 설명하고자 한다.
>
> SSE에서 제공하는 데이터의 종류는 관심사에 적합한 Channel 로 분류된다.

### OwnerChannel

##### 설명

- 가게 점주가 화면을 띄워놨을 때 수신 받는 데이터의 파이프라인을 나타낸다.

#### StoreOrderEvent

##### 설명

- 주문이 생성되었거나 주문의 상태 변환시 발생하는 이벤트이다.

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

### TableChannel

##### 설명

- 일반 고객이 테이블에 점유하고 있을 때 수신 받는 데이터의 파이프라인을 나타낸다.

#### TableOrderEvent

##### 설명

- 주문이 생성되었거나 주문의 상태 변환시 발생하는 이벤트이다.

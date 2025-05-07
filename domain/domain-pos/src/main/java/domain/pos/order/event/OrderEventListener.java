package domain.pos.order.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import domain.pos.order.implement.OrderMenuReader;
import domain.pos.order.implement.OrderWriter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
	private final OrderMenuReader orderMenuReader;
	private final OrderWriter orderWriter;

	@EventListener
	public void handleOrderEvent(OrderMenuStatusChangedEvent event) {
		if (!orderMenuReader.existsCookingMenu(event.getOrder().getOrderId())) {
			orderWriter.completeOrder(event.getOrder());
		}
	}
}

package domain.pos.order.implement;

import org.springframework.stereotype.Component;

import domain.pos.order.entity.Order;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

@Component
public interface StoreOrderProducer {
	void produceStoreOrder(Store store, Table table, Order order);
}

package com.pos.receipt.mapper;

import java.util.List;

import com.pos.menu.mapper.MenuMapper;
import com.pos.order.mapper.OrderMapper;
import com.pos.order.mapper.OrderMenuMapper;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.order.entity.Order;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.sale.entity.Sale;
import domain.pos.table.entity.Table;

public class ReceiptMapper {

	public static ReceiptEntity toReceiptEntity(Sale sale, Table table) {
		return ReceiptEntity.of(SaleEntity.from(sale.getId()), TableEntity.from(table.getTableId()));
	}

	public static Receipt toReceipt(ReceiptEntity receiptEntity, Table table, Sale sale) {
		return Receipt.of(
			toReceiptInfo(receiptEntity),
			sale,
			table);
	}

	public static ReceiptInfo toReceiptInfo(ReceiptEntity receiptEntity) {
		return ReceiptInfo.builder()
			.receiptId(receiptEntity.getId())
			.isAdjustment(receiptEntity.isAdjustment())
			.startUsageTime(receiptEntity.getStartUsageTime())
			.stopUsageTime(receiptEntity.getStopUsageTime())
			.occupancyFee(receiptEntity.getOccupancyFee())
			.build();
	}

	public static Receipt toReceiptWithMenus(ReceiptEntity receiptEntity) {
		List<Order> orders = receiptEntity.getOrders().stream()
			.map(orderEntity -> OrderMapper.toOrder(orderEntity, null,
				orderEntity.getOrderMenus().stream().map(
						orderMenuEntity -> OrderMenuMapper.toOrderMenu(orderMenuEntity, null,
							MenuMapper.toMenu(orderMenuEntity.getMenu(), null, null)))
					.toList()))
			.toList();

		Receipt receipt = Receipt.of(
			toReceiptInfo(receiptEntity),
			null,
			null);
		receipt.getOrders().addAll(orders);
		return receipt;
	}
}

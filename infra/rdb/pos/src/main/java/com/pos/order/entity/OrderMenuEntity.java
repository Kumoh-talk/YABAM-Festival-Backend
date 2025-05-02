package com.pos.order.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.menu.entity.MenuEntity;

import domain.pos.order.entity.OrderMenu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_menus")
@NoArgsConstructor
@SQLDelete(sql = "UPDATE order_menus SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class OrderMenuEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "quntity", nullable = false)
	private Integer quantity;

	// TODO : 메뉴 상태?

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@ManyToOne
	@Column(name = "menu_id", nullable = false)
	private MenuEntity menu;

	private OrderMenuEntity(Integer quantity, OrderEntity order, MenuEntity menu) {
		this.quantity = quantity;
		this.order = order;
		this.menu = menu;
	}

	public static OrderMenuEntity of(OrderMenu orderMenu) {
		return new OrderMenuEntity(
			orderMenu.getQuantity(),
			OrderEntity.from(orderMenu.getOrder().getOrderId()),
			MenuEntity.from(orderMenu.getOrderMenuId()));
	}

}

package com.pos.order.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.menu.entity.MenuEntity;

import domain.pos.order.entity.vo.OrderMenuStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class OrderMenuEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "quntity", nullable = false)
	private Integer quantity;

	@Enumerated(value = EnumType.STRING)
	private OrderMenuStatus status;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@ManyToOne
	@JoinColumn(name = "menu_id", nullable = false)
	private MenuEntity menu;

	private OrderMenuEntity(Integer quantity, OrderMenuStatus status, OrderEntity order, MenuEntity menu) {
		this.quantity = quantity;
		this.status = status;
		this.order = order;
		this.menu = menu;
	}

	public static OrderMenuEntity of(Integer quantity, OrderMenuStatus status, OrderEntity order, MenuEntity menu) {
		return new OrderMenuEntity(quantity, status, order, menu);
	}

}

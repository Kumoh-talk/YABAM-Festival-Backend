package com.pos.order.entity;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.order.entity.vo.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class OrderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(value = EnumType.STRING)
	private OrderStatus status;

	@Column(name = "table_price", nullable = false)
	private Integer totalPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receipt_id", nullable = false)
	private ReceiptEntity receipt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderMenuEntity> orderMenus;

	public OrderEntity(OrderStatus status, Integer totalPrice, ReceiptEntity receipt,
		List<OrderMenuEntity> orderMenus) {
		this.status = status;
		this.totalPrice = totalPrice;
		this.receipt = receipt;
		this.orderMenus = orderMenus;
	}

	public OrderEntity(Long id) {
		this.id = id;
	}

	public static OrderEntity from(Long id) {
		return new OrderEntity(id);
	}
}

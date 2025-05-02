package com.pos.receipt.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "receipt_customers")
@NoArgsConstructor
@SQLDelete(sql = "UPDATE receipt_customers SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class ReceiptCustomerEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@ManyToOne
	@JoinColumn(name = "receipt_id", nullable = false)
	private ReceiptEntity receipt;

	private ReceiptCustomerEntity(Long customerId, ReceiptEntity receipt) {
		this.customerId = customerId;
		this.receipt = receipt;
	}

	public static ReceiptCustomerEntity of(
		final Long customerId,
		final Long receiptId) {
		return new ReceiptCustomerEntity(customerId, ReceiptEntity.from(receiptId));
	}
}

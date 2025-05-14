package com.pos.call.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.sale.entity.SaleEntity;

import domain.pos.call.entity.CallMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calls")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CallEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Boolean isCompleted;

	@Column(nullable = false)
	private String message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receipt_id", nullable = false)
	private ReceiptEntity receipt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sale_id", nullable = false)
	private SaleEntity sale;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	private CallEntity(Boolean isCompleted, String message, ReceiptEntity receipt, SaleEntity sale) {
		this.isCompleted = isCompleted;
		this.message = message;
		this.receipt = receipt;
		this.sale = sale;
	}

	public static CallEntity of(ReceiptEntity receiptEntity, SaleEntity saleEntity, CallMessage callMessage) {
		return new CallEntity(
			callMessage.getIsComplete(),
			callMessage.getMessage(),
			receiptEntity,
			saleEntity
		);
	}

	public void completeCall() {
		this.isCompleted = true;
	}
}

package com.pos.receipt.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pos.global.base.entity.BaseEntity;
import com.pos.global.uuid.GeneratedUuidV7;
import com.pos.order.entity.OrderEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.table.entity.TableEntity;

import domain.pos.receipt.entity.ReceiptInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "receipts")
@NoArgsConstructor
@SQLDelete(sql = "UPDATE receipts SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
@Getter
public class ReceiptEntity extends BaseEntity {
	@Id
	@GeneratedUuidV7
	private UUID id;

	@Column(name = "is_adjustment", nullable = false)
	private boolean isAdjustment;

	@Column(name = "start_usage_time")
	private LocalDateTime startUsageTime;

	@Column(name = "stop_usage_time")
	private LocalDateTime stopUsageTime;

	@Column(name = "occupancy_fee")
	private Integer occupancyFee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sale_id", nullable = false)
	private SaleEntity sale;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id", nullable = false)
	private TableEntity table;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderEntity> orders = new ArrayList<>();

	@Builder
	private ReceiptEntity(boolean isAdjustment, LocalDateTime startUsageTime, LocalDateTime stopUsageTime,
		Integer occupancyFee, SaleEntity sale, TableEntity table) {
		this.isAdjustment = isAdjustment;
		this.startUsageTime = startUsageTime;
		this.stopUsageTime = stopUsageTime;
		this.occupancyFee = occupancyFee;
		this.sale = sale;
		this.table = table;
	}

	private ReceiptEntity(UUID id) {
		this.id = id;
	}

	public static ReceiptEntity of(
		SaleEntity saleEntity, TableEntity tableEntity) {
		return ReceiptEntity.builder()
			.sale(saleEntity)
			.table(tableEntity)
			.build();
	}

	public static ReceiptEntity from(
		final UUID receiptId) {
		return new ReceiptEntity(receiptId);
	}

	public void updateInfo(ReceiptInfo patchReceiptInfo) {
		this.isAdjustment = patchReceiptInfo.isAdjustment();
		this.stopUsageTime = patchReceiptInfo.getStopUsageTime();
		this.occupancyFee = patchReceiptInfo.getOccupancyFee();
	}
}

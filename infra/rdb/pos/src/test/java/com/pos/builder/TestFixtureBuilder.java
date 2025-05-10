package com.pos.builder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.cart.entity.CartEntity;
import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.entity.MenuEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.ReviewEntity;
import com.pos.sale.entity.SaleEntity;
import com.pos.store.entity.StoreDetailImageEntity;
import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;

@Component
public class TestFixtureBuilder {
	@Autowired
	private BuilderSupporter bs;

	public StoreEntity buildStoreEntity(StoreEntity storeEntity) {
		return bs.getStoreJpaRepository().save(storeEntity);
	}

	public List<TableEntity> buildTableEntityList(List<TableEntity> tableEntityList) {
		return bs.getTableJpaRepository().saveAll(tableEntityList);
	}

	public SaleEntity buildSaleEntity(SaleEntity saleEntity) {
		return bs.getSaleJpaRepository().save(saleEntity);
	}

	public MenuEntity buildMenuEntity(MenuEntity menuEntity) {
		return bs.getMenuJpaRepository().save(menuEntity);
	}

	public List<MenuEntity> buildMenuEntityList(List<MenuEntity> menuEntityList) {
		return bs.getMenuJpaRepository().saveAll(menuEntityList);
	}

	public MenuCategoryEntity buildMenuCategoryEntity(MenuCategoryEntity menuCategoryEntity) {
		return bs.getMenuCategoryJpaRepository().save(menuCategoryEntity);
	}

	public CartEntity buildCartEntity(CartEntity cartEntity) {
		return bs.getCartJpaRepository().save(cartEntity);
	}

	public List<StoreDetailImageEntity> buildStoreDetailImageEntities(
		List<StoreDetailImageEntity> storeDetailImageEntities) {
		return bs.getStoreDetailImageJpaRepository().saveAll(storeDetailImageEntities);
	}

	public ReviewEntity buildReviewEntity(ReviewEntity reviewEntity) {
		return bs.getReviewJpaRepository().save(reviewEntity);
	}

	public ReceiptEntity buildReceiptEntity(ReceiptEntity receiptEntity) {
		return bs.getReceiptJpaRepository().save(receiptEntity);
	}
}

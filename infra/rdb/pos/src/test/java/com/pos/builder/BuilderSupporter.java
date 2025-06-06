package com.pos.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pos.call.repository.jpa.CallJpaRepository;
import com.pos.cart.repository.CartJpaRepository;
import com.pos.menu.repository.jpa.MenuCategoryJpaRepository;
import com.pos.menu.repository.jpa.MenuJpaRepository;
import com.pos.receipt.repository.jpa.ReceiptJpaRepository;
import com.pos.review.repository.jpa.ReviewJpaRepository;
import com.pos.sale.repository.SaleJpaRepository;
import com.pos.store.repository.StoreDetailImageJpaRepository;
import com.pos.store.repository.StoreJpaRepository;
import com.pos.table.repository.TableJpaRepository;

@Component
public class BuilderSupporter {
	@Autowired
	private StoreJpaRepository storeJpaRepository;

	@Autowired
	private TableJpaRepository tableJpaRepository;

	@Autowired
	private SaleJpaRepository saleJpaRepository;

	@Autowired
	private MenuJpaRepository menuJpaRepository;

	@Autowired
	private CartJpaRepository cartJpaRepository;

	@Autowired
	private MenuCategoryJpaRepository menuCategoryJpaRepository;

	@Autowired
	private StoreDetailImageJpaRepository storeDetailImageJpaRepository;

	@Autowired
	private ReviewJpaRepository reviewJpaRepository;

	@Autowired
	private ReceiptJpaRepository receiptJpaRepository;

	@Autowired
	private CallJpaRepository callJpaRepository;

	public StoreJpaRepository getStoreJpaRepository() {
		return storeJpaRepository;
	}

	public TableJpaRepository getTableJpaRepository() {
		return tableJpaRepository;
	}

	public SaleJpaRepository getSaleJpaRepository() {
		return saleJpaRepository;
	}

	public MenuJpaRepository getMenuJpaRepository() {
		return menuJpaRepository;
	}

	public MenuCategoryJpaRepository getMenuCategoryJpaRepository() {
		return menuCategoryJpaRepository;
	}

	public CartJpaRepository getCartJpaRepository() {
		return cartJpaRepository;
	}

	public StoreDetailImageJpaRepository getStoreDetailImageJpaRepository() {
		return storeDetailImageJpaRepository;
	}

	public ReviewJpaRepository getReviewJpaRepository() {
		return reviewJpaRepository;
	}

	public ReceiptJpaRepository getReceiptJpaRepository() {
		return receiptJpaRepository;
	}

	public CallJpaRepository getCallJpaRepository() {
		return callJpaRepository;
	}
}

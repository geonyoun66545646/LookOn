package ks55team02.customer.cart.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
	
	private String gdsSttsNo; // 성훈이가 추가
	
	
	private String cartItemId;
	private String userNo;
	private String gdsNo;
	private String storeId; // cart_items.store_id (상점 ID)
	private String optNo;   // cart_items.opt_no (옵션 번호)
	private int quantity;
	private LocalDateTime addedAt;
	private LocalDateTime updatedAt;
	private boolean isChcOrd;

	// products 테이블에서 조인하여 가져오는 필드
	private String gdsNm;
	private BigDecimal gdsPrc; // products.last_sel_prc 매핑

	// product_options 테이블에서 조인하여 가져오는 필드
	private String optNm;
	// private BigDecimal optPrc; // product_options 테이블에 opt_prc 컬럼이 없으므로 제거

	public String getCartItemId() {
		return cartItemId;
	}
	public void setCartItemId(String cartItemId) {
		this.cartItemId = cartItemId;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getGdsNo() {
		return gdsNo;
	}
	public void setGdsNo(String gdsNo) {
		this.gdsNo = gdsNo;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getOptNo() {
		return optNo;
	}
	public void setOptNo(String optNo) {
		this.optNo = optNo;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public LocalDateTime getAddedAt() {
		return addedAt;
	}
	public void setAddedAt(LocalDateTime addedAt) {
		this.addedAt = addedAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	public boolean isChcOrd() {
		return isChcOrd;
	}
	public void setChcOrd(boolean isChcOrd) {
		this.isChcOrd = isChcOrd;
	}
	public String getGdsNm() {
		return gdsNm;
	}
	public void setGdsNm(String gdsNm) {
		this.gdsNm = gdsNm;
	}
	public BigDecimal getGdsPrc() {
		return gdsPrc;
	}
	public void setGdsPrc(BigDecimal gdsPrc) {
		this.gdsPrc = gdsPrc;
	}
	public String getOptNm() {
		return optNm;
	}
	public void setOptNm(String optNm) {
		this.optNm = optNm;
	}
	// public BigDecimal getOptPrc() { return optPrc; } // 제거
	// public void setOptPrc(BigDecimal optPrc) { this.optPrc = optPrc; } // 제거
	
}

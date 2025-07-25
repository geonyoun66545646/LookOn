package ks55team02.customer.cart.service;

import java.util.List;

import ks55team02.customer.cart.domain.CartDTO;

public interface CartService {
	
	List<CartDTO> getCartItemsByUserNo(String userNo);
	/**
	     * 장바구니에 상품 추가 또는 수량 증가
	     * @param userNo 사용자 번호
	     * @param gdsNo 상품 번호
	     * @param quantity 추가할 수량
	     * @param gdsSttsNo 상품 상태 번호 (nullable) ⭐수정됨
	     * @param storeId 상점 ID
	     */
	void addProductToCart(String userNo, String gdsNo, int quantity, String gdsSttsNo, String storeId);
	
	void updateCartItemQuantity(String userNo, String cartItemId, int quantity);
	
	void removeCartItem(String userNo, String cartItemId);
	
	void clearCart(String userNo);
	
	void removeCartItemsByIds(String userNo, List<String> cartItemIds);
	
	/**
     * 특정 사용자의 장바구니에 담긴 상품의 총 개수를 조회합니다.
     * 헤더의 장바구니 아이콘 뱃지에 숫자를 표시하기 위해 사용됩니다.
     * 
     * @param userNo 조회할 사용자의 고유 번호
     * @return 장바구니에 담긴 상품의 총 개수
     */
	int getCartItemCount(String userNo);
	
}
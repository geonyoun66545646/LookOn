package ks55team02.customer.cart.service;

import java.util.List;

import ks55team02.customer.cart.domain.CartDTO;

//@Service
public interface CartService {

	/**
     * 사용자 번호로 장바구니 목록 조회
     * @param userNo 사용자 번호
     * @return 해당 사용자의 장바구니 항목 목록
     */
    List<CartDTO> getCartItemsByUserNo(String userNo);

    /**
     * 장바구니에 상품 추가 또는 수량 증가
     * @param userNo 사용자 번호
     * @param gdsNo 상품 번호
     * @param quantity 추가할 수량
     * @param optNo 옵션 번호 (nullable)
     * @param storeId 상점 ID
     */
    void addProductToCart(String userNo, String gdsNo, int quantity, String optNo, String storeId);

    /**
     * 장바구니 항목의 수량 변경
     * @param userNo 사용자 번호 (소유권 확인용)
     * @param cartItemId 장바구니 항목 ID
     * @param quantity 변경할 수량
     */
    void updateCartItemQuantity(String userNo, String cartItemId, int quantity);

    /**
     * 장바구니 항목 삭제
     * @param userNo 사용자 번호 (소유권 확인용)
     * @param cartItemId 삭제할 장바구니 항목 ID
     */
    void removeCartItem(String userNo, String cartItemId);

    /**
     * 사용자 장바구니의 모든 항목 비우기
     * @param userNo 사용자 번호
     */
    void clearCart(String userNo);
    
  
}

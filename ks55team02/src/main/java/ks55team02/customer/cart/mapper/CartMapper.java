package ks55team02.customer.cart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.cart.domain.CartDTO;

@Mapper
public interface CartMapper {
	/**
     * 사용자 번호로 장바구니 목록 조회
     * @param userNo 사용자 번호
     * @return 해당 사용자의 장바구니 항목 목록
     */
    List<CartDTO> selectCartItemsByUserNo(String userNo);

    /**
     * 특정 사용자의 장바구니에 이미 동일한 상품(옵션 포함)이 있는지 조회
     * @param userNo 사용자 번호
     * @param gdsNo 상품 번호
     * @param optNo 옵션 번호 (nullable)
     * @param storeId 상점 ID
     * @return 존재하는 경우 CartItem 객체, 없으면 null
     */
    CartDTO getExistingCartItem(@Param("userNo") String userNo,
                                 @Param("gdsNo") String gdsNo,
                                 @Param("optNo") String optNo,
                                 @Param("storeId") String storeId);

    /**
     * 새로운 장바구니 항목 추가
     * @param cartItem 추가할 CartItem 객체
     */
    void insertCartItem(CartDTO cartItem);

    /**
     * 장바구니 항목의 수량 업데이트 (PK 기반)
     * @param cartItem 업데이트할 CartItem 객체 (cartItemId, quantity, updatedAt 필요)
     */
    void updateCartItemQuantity(CartDTO cartItem);

    /**
     * 장바구니 항목 ID로 단일 항목 조회 (수량 변경/삭제 시 유효성 검증용)
     * @param cartItemId 장바구니 항목 ID
     * @return 조회된 CartItem 객체
     */
    CartDTO getCartItemById(String cartItemId);

    /**
     * 장바구니 항목 삭제 (PK 기반)
     * @param cartItemId 삭제할 장바구니 항목 ID
     */
    void deleteCartItem(String cartItemId);

    /**
     * 사용자 번호로 장바구니의 모든 항목 삭제
     * @param userNo 사용자 번호
     */
    void clearCartByUserNo(String userNo);
    
    // TODO: 추가할 메서드 - 가장 큰 cart_item_id의 숫자 부분 조회
    String getMaxCartItemId(); // 예: "cart_id_80" 또는 "80"을 반환하도록 처리
}

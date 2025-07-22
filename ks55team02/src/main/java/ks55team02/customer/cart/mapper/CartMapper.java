package ks55team02.customer.cart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.cart.domain.CartDTO;

@Mapper
public interface CartMapper {
	List<CartDTO> selectCartItemsByUserNo(String userNo);

	/**
	 *      * 특정 사용자의 장바구니에 이미 동일한 상품(옵션 포함)이 있는지 조회      * @param userNo 사용자 번호    
	 *  * @param gdsNo 상품 번호      * @param gdsSttsNo 상품 상태 번호 (nullable) ⭐수정됨    
	 *  * @param storeId 상점 ID      * @return 존재하는 경우 CartItem 객체, 없으면 null      
	 */
	CartDTO getExistingCartItem(@Param("userNo") String userNo, @Param("gdsNo") String gdsNo,
			@Param("gdsSttsNo") String gdsSttsNo, // ⭐최종 수정된 부분
			@Param("storeId") String storeId);

	void insertCartItem(CartDTO cartItem);

	void updateCartItemQuantity(CartDTO cartItem);

	CartDTO getCartItemById(String cartItemId);

	void deleteCartItem(String cartItemId);

	void clearCartByUserNo(String userNo);

	String getMaxCartItemId();

	void deleteCartItemsByIds(@Param("cartItemIds") List<String> cartItemIds, @Param("userNo") String userNo);
	
	int getCartItemCount(@Param("userNo") String userNo);
}
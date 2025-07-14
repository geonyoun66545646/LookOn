package ks55team02.customer.cart.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.cart.domain.CartDTO; // CartDTO로 변경
import ks55team02.customer.cart.mapper.CartMapper;
import ks55team02.customer.cart.service.CartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartServiceImpl implements CartService{

	private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartMapper cartMapper;

    /**
     * 사용자 번호로 장바구니 목록 조회
     * @param userNo 사용자 번호
     * @return 해당 사용자의 장바구니 항목 목록 (CartDTO 타입)
     */
    @Override
    public List<CartDTO> getCartItemsByUserNo(String userNo) {
        log.info("서비스: 사용자 '{}'의 장바구니 조회", userNo);
        return cartMapper.selectCartItemsByUserNo(userNo);
    }

    /**
     * 장바구니에 상품 추가 또는 수량 증가
     * @param userNo 사용자 번호
     * @param gdsNo 상품 번호
     * @param quantity 추가할 수량
     * @param optNo 옵션 번호 (nullable)
     * @param storeId 상점 ID
     */
    @Override
    public void addProductToCart(String userNo, String gdsNo, int quantity, String optNo, String storeId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("상품 수량은 1 이상이어야 합니다.");
        }

        log.info("서비스: 사용자 '{}' 장바구니에 상품 '{}' 추가 시도. 수량: {}", userNo, gdsNo, quantity);

        // 1. 해당 상품(옵션 포함)이 이미 장바구니에 있는지 확인
        CartDTO existingItem = cartMapper.getExistingCartItem(userNo, gdsNo, optNo, storeId);

        if (existingItem != null) {
            // 2. 이미 존재하는 경우: 수량 업데이트
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setUpdatedAt(LocalDateTime.now()); // camelCase 필드 접근
            cartMapper.updateCartItemQuantity(existingItem);
            log.info("서비스: 장바구니 항목 '{}' 수량 업데이트 ({} -> {})", existingItem.getCartItemId(), existingItem.getQuantity() - quantity, newQuantity); // camelCase 필드 접근
        } else {
            // 3. 존재하지 않는 경우: 새로운 항목 추가
        	CartDTO newCartDTO = new CartDTO();
        	newCartDTO.setCartItemId(UUID.randomUUID().toString()); // camelCase 필드 접근
        	newCartDTO.setUserNo(userNo); // camelCase 필드 접근
        	newCartDTO.setGdsNo(gdsNo); // camelCase 필드 접근
        	newCartDTO.setStoreId(storeId); // camelCase 필드 접근
        	newCartDTO.setOptNo(optNo); // camelCase 필드 접근
        	newCartDTO.setQuantity(quantity);
        	newCartDTO.setAddedAt(LocalDateTime.now()); // camelCase 필드 접근
        	newCartDTO.setUpdatedAt(LocalDateTime.now()); // camelCase 필드 접근
            newCartDTO.setChcOrd(true); // camelCase 필드 접근 (boolean getter는 isChcOrd(), setter는 setChcOrd())

            cartMapper.insertCartItem(newCartDTO);
            log.info("서비스: 사용자 '{}' 장바구니에 새 상품 '{}' 추가. ID: {}", userNo, gdsNo, newCartDTO.getCartItemId()); // camelCase 필드 접근
        }
    }

    /**
     * 장바구니 항목의 수량 변경
     * @param userNo 사용자 번호 (소유권 확인용)
     * @param cartItemId 장바구니 항목 ID
     * @param quantity 변경할 수량
     */
    @Override
    public void updateCartItemQuantity(String userNo, String cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        // 1. 해당 cartItemId가 현재 userNo의 장바구니 항목인지 확인 (보안 강화)
        CartDTO cartItemToUpdate = cartMapper.getCartItemById(cartItemId);
        if (cartItemToUpdate == null || !cartItemToUpdate.getUserNo().equals(userNo)) { // camelCase 필드 접근
            throw new IllegalArgumentException("유효하지 않거나 접근 권한이 없는 장바구니 항목입니다.");
        }

        log.info("서비스: 장바구니 항목 '{}' 수량 변경 시도. 새 수량: {}", cartItemId, quantity);
        cartItemToUpdate.setQuantity(quantity);
        cartItemToUpdate.setUpdatedAt(LocalDateTime.now()); // camelCase 필드 접근
        cartMapper.updateCartItemQuantity(cartItemToUpdate);
        log.info("서비스: 장바구니 항목 '{}' 수량 변경 성공. 새 수량: {}", cartItemId, quantity);
    }

    /**
     * 장바구니 항목 삭제
     * @param userNo 사용자 번호 (소유권 확인용)
     * @param cartItemId 삭제할 장바구니 항목 ID
     */
    @Override
    public void removeCartItem(String userNo, String cartItemId) {
        // 1. 해당 cartItemId가 현재 userNo의 장바구니 항목인지 확인 (보안 강화)
        CartDTO cartItemToDelete = cartMapper.getCartItemById(cartItemId);
        if (cartItemToDelete == null || !cartItemToDelete.getUserNo().equals(userNo)) { // camelCase 필드 접근
            throw new IllegalArgumentException("유효하지 않거나 접근 권한이 없는 장바구니 항목입니다.");
        }

        log.info("서비스: 장바구니 항목 '{}' 삭제 시도", cartItemId);
        cartMapper.deleteCartItem(cartItemId);
        log.info("서비스: 장바구니 항목 '{}' 삭제 성공", cartItemId);
    }

    /**
     * 사용자 장바구니의 모든 항목 비우기
     * @param userNo 사용자 번호
     */
    @Override
    public void clearCart(String userNo) {
        log.info("서비스: 사용자 '{}'의 장바구니 비우기 시도", userNo);
        cartMapper.clearCartByUserNo(userNo);
        log.info("서비스: 사용자 '{}'의 장바구니 비우기 성공", userNo);
    }
}

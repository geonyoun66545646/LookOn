package ks55team02.customer.cart.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.cart.domain.CartDTO;
import ks55team02.customer.cart.mapper.CartMapper;
import ks55team02.customer.cart.service.CartService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartMapper cartMapper;

    public CartServiceImpl(CartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    @Override
    public void addProductToCart(String userNo, String gdsNo, int quantity, String gdsSttsNo, String storeId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("상품 수량은 1 이상이어야 합니다.");
        }

        log.info("서비스: 사용자 '{}' 장바구니에 상품 '{}' 추가 시도. 수량: {}", userNo, gdsNo, quantity);

        // 1. 해당 상품(옵션 포함)이 이미 장바구니에 있는지 확인
        CartDTO existingItem = cartMapper.getExistingCartItem(userNo, gdsNo, gdsSttsNo, storeId);

        if (existingItem != null) {
            // 2. 이미 존재하는 경우: 수량 업데이트
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateCartItemQuantity(existingItem);
            log.info("서비스: 장바구니 항목 '{}' 수량 업데이트 ({} -> {})", existingItem.getCartItemId(), existingItem.getQuantity() - quantity, newQuantity);
        } else {
            // 3. 존재하지 않는 경우: 새로운 항목 추가
            CartDTO newCartDTO = new CartDTO();
            newCartDTO.setCartItemId(generateNewCartItemId());
            newCartDTO.setUserNo(userNo);
            newCartDTO.setGdsNo(gdsNo);
            newCartDTO.setStoreId(storeId);
            newCartDTO.setGdsSttsNo(gdsSttsNo);
            newCartDTO.setQuantity(quantity);
            newCartDTO.setAddedAt(LocalDateTime.now());
            newCartDTO.setUpdatedAt(LocalDateTime.now());
            newCartDTO.setChcOrd(true);

            cartMapper.insertCartItem(newCartDTO);
            log.info("서비스: 사용자 '{}' 장바구니에 새 상품 '{}' 추가. ID: {}", userNo, gdsNo, newCartDTO.getCartItemId());
        }
    }

    @Override
    public void updateCartItemQuantity(String userNo, String cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        CartDTO cartItemToUpdate = cartMapper.getCartItemById(cartItemId);
        if (cartItemToUpdate == null || !cartItemToUpdate.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("유효하지 않거나 접근 권한이 없는 장바구니 항목입니다.");
        }
        log.info("서비스: 장바구니 항목 '{}' 수량 변경 시도. 새 수량: {}", cartItemId, quantity);
        cartItemToUpdate.setQuantity(quantity);
        cartItemToUpdate.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCartItemQuantity(cartItemToUpdate);
        log.info("서비스: 장바구니 항목 '{}' 수량 변경 성공. 새 수량: {}", cartItemId, quantity);
    }

    @Override
    public void removeCartItem(String userNo, String cartItemId) {
        CartDTO cartItemToDelete = cartMapper.getCartItemById(cartItemId);
        if (cartItemToDelete == null || !cartItemToDelete.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("유효하지 않거나 접근 권한이 없는 장바구니 항목입니다.");
        }
        log.info("서비스: 장바구니 항목 '{}' 삭제 시도", cartItemId);
        cartMapper.deleteCartItem(cartItemId);
        log.info("서비스: 장바구니 항목 '{}' 삭제 성공", cartItemId);
    }

    @Override
    public void clearCart(String userNo) {
        log.info("서비스: 사용자 '{}'의 장바구니 비우기 시도", userNo);
        cartMapper.clearCartByUserNo(userNo);
        log.info("서비스: 사용자 '{}'의 장바구니 비우기 성공", userNo);
    }

    private String generateNewCartItemId() {
        String maxIdStr = cartMapper.getMaxCartItemId();
        int nextNum;
        if (maxIdStr != null) {
            try {
                nextNum = Integer.parseInt(maxIdStr) + 1;
            } catch (NumberFormatException e) {
                log.error("getMaxCartItemId 결과가 숫자로 변환될 수 없습니다: {}", maxIdStr, e);
                nextNum = 81;
            }
        } else {
            nextNum = 81;
        }
        return "cart_id_" + nextNum;
    }

    @Override
    public List<CartDTO> getCartItemsByUserNo(String userNo) {
        log.info("서비스: 사용자 '{}'의 장바구니 조회", userNo);
        return cartMapper.selectCartItemsByUserNo(userNo);
    }

    @Override
    public void removeCartItemsByIds(String userNo, List<String> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            log.warn("서비스: 삭제할 장바구니 항목 ID 목록이 비어 있습니다. 사용자: {}", userNo);
            return;
        }
        log.info("서비스: 사용자 '{}'의 장바구니 항목들 삭제 시도: {}", userNo, cartItemIds);
        cartMapper.deleteCartItemsByIds(cartItemIds, userNo);
        log.info("서비스: 사용자 '{}'의 장바구니 항목들 삭제 성공", userNo);
    }
    
    /**
     * CartService 인터페이스에 선언된 getCartItemCount 메소드를 구현합니다.
     * CartMapper를 호출하여 실제 데이터베이스 조회를 수행합니다.
     */
    @Override
    public int getCartItemCount(String userNo) {
    	
        log.info("서비스: 사용자 '{}'의 장바구니 수량 조회를 위해 Mapper를 호출합니다.", userNo);
        return cartMapper.getCartItemCount(userNo);
    }
}

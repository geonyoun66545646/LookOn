package ks55team02.customer.cart.cartcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.cart.domain.CartDTO;
import ks55team02.customer.cart.service.CartService;
import ks55team02.customer.login.domain.LoginUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;
    
    // 장바구니 목록 조회 (변경 없음)
    @GetMapping
    public ResponseEntity<List<CartDTO>> getCartItems(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(cartService.getCartItemsByUserNo(loginUser.getUserNo()));
    }

    // 장바구니 상품 추가 (변경 없음)
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(@RequestBody Map<String, Object> payload, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        String userNo = loginUser.getUserNo();
        String gdsNo = (String) payload.get("gdsNo");
        String storeId = (String) payload.get("storeId");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectedOptions = (List<Map<String, Object>>) payload.get("selectedOptions");

        if (gdsNo == null || selectedOptions == null || selectedOptions.isEmpty()) {
            return new ResponseEntity<>("상품 번호 및 선택된 옵션이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            for (Map<String, Object> option : selectedOptions) {
                // ⭐⭐ 이 부분을 수정합니다. ⭐⭐
                String gdsSttsNo = (String) option.get("gdsSttsNo"); // "optNo" -> "gdsSttsNo"
                Integer quantity = (Integer) option.get("quantity");
                
                // ⭐ gdsSttsNo로 변경되었으므로, addProductToCart 메서드 호출 시 gdsSttsNo를 전달해야 합니다.
                //    기존 addProductToCart가 optNo를 받는다면, gdsSttsNo를 받도록 서비스/매퍼 수정이 필요할 수 있습니다.
                //    우선은 gdsSttsNo를 사용하도록 변경합니다.
                if (gdsSttsNo != null && quantity != null && quantity > 0) {
                    // cartService.addProductToCart가 4개의 파라미터를 받는다고 가정
                    cartService.addProductToCart(userNo, gdsNo, quantity, gdsSttsNo, storeId);
                }
            }
            return ResponseEntity.ok("상품이 장바구니에 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            log.error("장바구니 추가 중 서버 오류 발생 (사용자: {}): {}", userNo, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 추가 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 장바구니 항목 수량 변경
     * @param payload Map으로 cartItemId, quantity 수신
     */
    @PutMapping("/updateQuantity")
    public ResponseEntity<String> updateCartItemQuantity(@RequestBody Map<String, Object> payload, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        String userNo = loginUser.getUserNo();
        String cartItemId = (String) payload.get("cartItemId");
        Integer quantity = (Integer) payload.get("quantity");

        if (cartItemId == null || quantity == null || quantity <= 0) {
            return new ResponseEntity<>("장바구니 항목 ID와 유효한 수량이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            cartService.updateCartItemQuantity(userNo, cartItemId, quantity);
            return ResponseEntity.ok("장바구니 수량이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 권한 없음 오류
        } catch (Exception e) {
            log.error("장바구니 수량 변경 중 서버 오류 발생 (사용자: {}, 항목: {}): {}", userNo, cartItemId, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 수량 변경 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * [수정됨] 장바구니 항목 삭제
     * @param cartItemId 삭제할 장바구니 항목 ID
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeCartItem(@PathVariable String cartItemId, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        String userNo = loginUser.getUserNo();
        try {
            cartService.removeCartItem(userNo, cartItemId);
            return ResponseEntity.ok("장바구니 항목이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 권한 없음 오류
        } catch (Exception e) {
            log.error("장바구니 항목 삭제 중 서버 오류 발생 (사용자: {}, 항목: {}): {}", userNo, cartItemId, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 삭제 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 장바구니 비우기 (변경 없음)
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        cartService.clearCart(loginUser.getUserNo());
        return ResponseEntity.ok("장바구니가 성공적으로 비워졌습니다.");
    }
    
    /**
     * [추가] 결제된 장바구니 항목들 일괄 삭제
     * @param cartItemIds 삭제할 장바구니 항목 ID 목록
     */
    @PostMapping("/removeSelected") // 배열 형태의 데이터를 받기 위해 POST 사용
    public ResponseEntity<String> removeSelectedCartItems(@RequestBody List<String> cartItemIds, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        String userNo = loginUser.getUserNo();
        
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return new ResponseEntity<>("삭제할 장바구니 항목 ID가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            cartService.removeCartItemsByIds(userNo, cartItemIds);
            return ResponseEntity.ok("결제된 장바구니 항목들이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 권한 없음 오류
        } catch (Exception e) {
            log.error("선택된 장바구니 항목 삭제 중 서버 오류 발생 (사용자: {}, 항목: {}): {}", userNo, cartItemIds, e.getMessage(), e);
            return new ResponseEntity<>("선택된 장바구니 항목 삭제 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
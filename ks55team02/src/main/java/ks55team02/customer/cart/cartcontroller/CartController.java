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
import ks55team02.customer.login.domain.LoginUser; // LoginUser DTO 임포트 추가

import java.util.List;
import java.util.Map;

@RestController // REST API만 제공하므로 @RestController가 더 적합합니다.
@RequestMapping("/api/cart") // API 기본 경로 설정
public class CartController {
	
	private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;
    
    // 장바구니 뷰 페이지를 반환하는 메서드는 CartViewController 등으로 분리되어야 합니다.
    // 이 컨트롤러는 /api/cart로 시작하는 API 요청만 처리합니다.

    /**
     * 현재 로그인한 사용자의 장바구니 목록 조회
     * GET /api/cart
     * @param session HTTP 세션 (LoginUser 객체로부터 사용자 번호 획득)
     * @return 장바구니 목록 및 HTTP 상태
     */
    @GetMapping // GET /api/cart 요청 처리
    public ResponseEntity<List<CartDTO>> getCartItems(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("장바구니 조회: 로그인되지 않은 사용자 요청");
            // 장바구니 페이지에서 로그인 확인은 이미 프론트엔드에서 처리되므로,
            // 여기서는 빈 목록을 반환하거나 Unauthorized를 반환할 수 있습니다.
            // 클라이언트에서 로그인 여부를 먼저 체크하므로, 여기서는 빈 리스트를 반환하는 것이 UI 흐름상 자연스러울 수 있습니다.
            return new ResponseEntity<>(List.of(), HttpStatus.OK); // 빈 리스트 반환
            // 또는 return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }
        String userNo = loginUser.getUserNo();
        List<CartDTO> cartItems = cartService.getCartItemsByUserNo(userNo);
        log.info("사용자 '{}'의 장바구니 조회: {}개 항목", userNo, cartItems.size());
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    /**
     * 장바구니에 상품 추가 (또는 기존 상품 수량 증가)
     * POST /api/cart/add
     * @param requestBody JSON 요청 본문 (gdsNo, quantity, optNo, storeId)
     * @param session HTTP 세션 (LoginUser 객체로부터 사용자 번호 획득)
     * @return 성공/실패 여부 및 HTTP 상태
     */
    @PostMapping("/add") // POST /api/cart/add
    public ResponseEntity<String> addProductToCart(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        // 세션에서 LoginUser 객체를 가져와 userNo를 추출합니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("장바구니 추가: 로그인되지 않은 사용자 요청 (세션에 loginUser 없음)");
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답
        }
        String userNo = loginUser.getUserNo(); // LoginUser 객체에서 userNo 추출

        try {
            String gdsNo = (String) requestBody.get("gdsNo");
            int quantity = (Integer) requestBody.getOrDefault("quantity", 1); // 기본값 1
            String optNo = (String) requestBody.get("optNo"); // 옵션 번호 (nullable)
            String storeId = (String) requestBody.get("storeId"); // 상점 ID (필수)

            // 필수 파라미터 유효성 검사
            if (gdsNo == null || storeId == null) {
                log.warn("장바구니 추가: 필수 파라미터 누락 (userNo: {}, gdsNo: {}, storeId: {})", userNo, gdsNo, storeId);
                return new ResponseEntity<>("필수 상품 정보가 누락되었습니다.", HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
            }

            cartService.addProductToCart(userNo, gdsNo, quantity, optNo, storeId);
            log.info("사용자 '{}' 장바구니에 상품 '{}' 추가/업데이트 성공. 수량: {}", userNo, gdsNo, quantity);
            return new ResponseEntity<>("장바구니에 상품이 추가되었습니다.", HttpStatus.OK); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.warn("장바구니 추가 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
        } catch (Exception e) {
            log.error("장바구니 상품 추가 중 오류 발생 (사용자: {}, 상품: {}): {}", userNo, requestBody, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 추가 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error 응답
        }
    }

    /**
     * 장바구니 항목 수량 변경
     * PUT /api/cart/{cartItemId}
     * @param cartItemId 변경할 장바구니 항목 ID
     * @param requestBody JSON 요청 본문 (quantity)
     * @param session HTTP 세션 (LoginUser 객체로부터 사용자 번호 획득)
     * @return 성공/실패 여부 및 HTTP 상태
     */
    @PutMapping("/{cartItemId}") // PUT /api/cart/{cartItemId}
    public ResponseEntity<String> updateCartItemQuantity(@PathVariable String cartItemId,
                                                        @RequestBody Map<String, Integer> requestBody,
                                                        HttpSession session) {
        // 세션에서 LoginUser 객체를 가져와 userNo를 추출합니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("장바구니 수량 변경: 로그인되지 않은 사용자 요청 (세션에 loginUser 없음)");
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답
        }
        String userNo = loginUser.getUserNo(); // LoginUser 객체에서 userNo 추출

        try {
            int quantity = requestBody.get("quantity");
            if (quantity <= 0) {
                return new ResponseEntity<>("수량은 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
            }
            cartService.updateCartItemQuantity(userNo, cartItemId, quantity);
            log.info("사용자 '{}' 장바구니 항목 '{}' 수량 변경 성공. 새 수량: {}", userNo, cartItemId, quantity);
            return new ResponseEntity<>("장바구니 수량이 변경되었습니다.", HttpStatus.OK); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.warn("장바구니 수량 변경 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
        } catch (Exception e) {
            log.error("장바구니 수량 변경 중 오류 발생 (사용자: {}, 항목: {}): {}", userNo, cartItemId, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 수량 변경 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error 응답
        }
    }

    /**
     * 장바구니 항목 삭제
     * DELETE /api/cart/{cartItemId}
     * @param cartItemId 삭제할 장바구니 항목 ID
     * @param session HTTP 세션 (LoginUser 객체로부터 사용자 번호 획득)
     * @return 성공/실패 여부 및 HTTP 상태
     */
    @DeleteMapping("/{cartItemId}") // DELETE /api/cart/{cartItemId}
    public ResponseEntity<String> removeCartItem(@PathVariable String cartItemId, HttpSession session) {
        // 세션에서 LoginUser 객체를 가져와 userNo를 추출합니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("장바구니 삭제: 로그인되지 않은 사용자 요청 (세션에 loginUser 없음)");
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답
        }
        String userNo = loginUser.getUserNo(); // LoginUser 객체에서 userNo 추출

        try {
            cartService.removeCartItem(userNo, cartItemId);
            log.info("사용자 '{}' 장바구니 항목 '{}' 삭제 성공.", userNo, cartItemId);
            return new ResponseEntity<>("장바구니에서 상품이 삭제되었습니다.", HttpStatus.OK); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.warn("장바구니 삭제 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
        } catch (Exception e) {
            log.error("장바구니 항목 삭제 중 오류 발생 (사용자: {}, 항목: {}): {}", userNo, cartItemId, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 삭제 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error 응답
        }
    }

    /**
     * 장바구니 모든 항목 비우기
     * DELETE /api/cart/clear
     * @param session HTTP 세션 (LoginUser 객체로부터 사용자 번호 획득)
     * @return 성공/실패 여부 및 HTTP 상태
     */
    @DeleteMapping("/clear") // DELETE /api/cart/clear
    public ResponseEntity<String> clearCart(HttpSession session) {
        // 세션에서 LoginUser 객체를 가져와 userNo를 추출합니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("장바구니 비우기: 로그인되지 않은 사용자 요청 (세션에 loginUser 없음)");
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답
        }
        String userNo = loginUser.getUserNo(); // LoginUser 객체에서 userNo 추출

        try {
            cartService.clearCart(userNo);
            log.info("사용자 '{}'의 장바구니가 비워졌습니다.", userNo);
            return new ResponseEntity<>("장바구니가 비워졌습니다.", HttpStatus.OK); // 200 OK 응답
        } catch (Exception e) {
            log.error("장바구니 비우기 중 오류 발생 (사용자: {}): {}", userNo, e.getMessage(), e);
            return new ResponseEntity<>("장바구니 비우기 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error 응답
        }
    }
}

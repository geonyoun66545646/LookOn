package ks55team02.customer.coupons.controller;

import ks55team02.util.CustomerPagination;
import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.service.CouponsService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponsApiController {

	private final CouponsService couponsService;

	/**
	 * '쿠폰 받기' 탭의 쿠폰 목록을 조회하는 API
	 */
	@GetMapping("/available")
	public ResponseEntity<CustomerPagination<Coupons>> getAvailableCoupons(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "recent") String sortOrder,
			@RequestParam(value = "page", defaultValue = "1") int page, HttpSession session) {

		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

		if (loginUser == null) {
			// ★★★ 이 부분을 수정합니다. 로그인되지 않은 사용자에게 401 Unauthorized를 반환합니다. ★★★
			log.warn("API 호출 - 발급 가능 쿠폰 조회: 비로그인 사용자. 401 Unauthorized 반환.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 상태 코드만 반환
		}

		String userNo = loginUser.getUserNo();

		log.info("API 호출 - 발급 가능 쿠폰 조회: userNo={}, keyword={}, page={}", userNo, keyword, page);
		CustomerPagination<Coupons> availableCouponsPage = couponsService.getAvailableCoupons(userNo, keyword,
				sortOrder, page);
		return new ResponseEntity<>(availableCouponsPage, HttpStatus.OK);
	}

	/**
	 * '보유 쿠폰' 탭의 쿠폰 목록을 조회하는 API
	 */
	@GetMapping("/my")
	public ResponseEntity<CustomerPagination<UserCoupons>> getMyCoupons(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "recent") String sortOrder,
			@RequestParam(value = "page", defaultValue = "1") int page, HttpSession session) {

		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String userNo = loginUser.getUserNo();

		log.info("API 호출 - 보유 쿠폰 조회: userNo={}, keyword={}, page={}", userNo, keyword, sortOrder, page);
		CustomerPagination<UserCoupons> myCouponsPage = couponsService.getMyCoupons(userNo, keyword, sortOrder, page);

		return new ResponseEntity<>(myCouponsPage, HttpStatus.OK);
	}

	/**
	 * 쿠폰 발급을 처리하는 API (최종 수정본)
	 */
	@PostMapping("/{couponId}/issue")
	public ResponseEntity<Map<String, String>> issueCoupon(@PathVariable String couponId, HttpSession session) {
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 후 이용해주세요."));
		}

		String userNo = loginUser.getUserNo();

		log.info("API 호출 - 쿠폰 발급: userNo={}, couponId={}", userNo, couponId);

		try {
			boolean isSuccess = couponsService.issueCouponToUser(userNo, couponId);
			if (isSuccess) {
				return ResponseEntity.ok(Map.of("message", "쿠폰이 발급되었습니다."));
			} else {
				// isSuccess가 false인 경우는 Service에서 이미 예외를 던지므로 이 else 블록은 일반적으로 실행되지 않음.
				// 하지만 방어적으로 남겨두거나, 서비스 계층에서 boolean 대신 Map이나 커스텀 DTO를 반환하도록 변경할 수도 있음.
				return ResponseEntity.badRequest().body(Map.of("message", "알 수 없는 이유로 쿠폰 발급에 실패했습니다."));
			}
		} catch (IllegalArgumentException e) { // Service에서 던진 특정 예외를 잡음
			log.error("쿠폰 발급 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); // 예외 메시지를 그대로 반환
		} catch (Exception e) {
			log.error("쿠폰 발급 중 시스템 오류 발생: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "쿠폰 발급 중 시스템 오류가 발생했습니다."));
		}
	}
}
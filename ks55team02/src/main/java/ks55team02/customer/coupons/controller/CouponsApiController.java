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
@RequestMapping("/customer/api/coupons") // API 경로 일관성 유지
@RequiredArgsConstructor
@Slf4j
public class CouponsApiController {

	private final CouponsService couponsService;

	/**
	 * (수정) '쿠폰 받기' 탭의 모든 사용자가 볼 수 있는 쿠폰 목록 API
	 */
	@GetMapping("/available")
	public ResponseEntity<CustomerPagination<Coupons>> getAvailableCoupons(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "recent") String sortOrder,
			@RequestParam(value = "page", defaultValue = "1") int page, HttpSession session) { // HttpSession 추가

		// (추가) 세션에서 사용자 정보 조회
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		String userNo = (loginUser != null) ? loginUser.getUserNo() : null;

		log.info("API 호출 - 발급 가능 쿠폰 목록 조회: userNo={}, keyword={}, page={}", userNo, keyword, page);
		CustomerPagination<Coupons> availableCouponsPage = couponsService.getAvailableCoupons(userNo, keyword,
				sortOrder, page); // userNo 전달
		return ResponseEntity.ok(availableCouponsPage);
	}

	/**
	 * (수정) '보유 쿠폰' 탭의 쿠폰 목록을 조회하는 API (필터링 기능 추가)
	 */
	@GetMapping("/my")
	public ResponseEntity<CustomerPagination<UserCoupons>> getMyCoupons(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "recent") String sortOrder,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "isUsed", required = false) Boolean isUsed, // (추가) 사용 여부 필터링
			HttpSession session) {

		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String userNo = loginUser.getUserNo();

		log.info("API 호출 - 보유 쿠폰 조회: userNo={}, keyword={}, sortOrder={}, page={}, isUsed={}", userNo, keyword,
				sortOrder, page, isUsed);
		// (수정) isUsed 파라미터를 서비스에 전달
		CustomerPagination<UserCoupons> myCouponsPage = couponsService.getMyCoupons(userNo, keyword, sortOrder, page,
				isUsed);
		return ResponseEntity.ok(myCouponsPage);
	}

	@PostMapping("/{couponId}/issue")
	public ResponseEntity<Map<String, String>> issueCoupon(@PathVariable String couponId, HttpSession session) {
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 후 이용해주세요."));
		}
		String userNo = loginUser.getUserNo();
		log.info("API 호출 - 쿠폰 발급 시도: userNo={}, couponId={}", userNo, couponId);

		try {
			// (수정) 서비스는 이제 Map을 반환합니다.
			Map<String, Object> result = couponsService.issueCouponToUser(userNo, couponId);
			boolean isSuccess = (boolean) result.get("success");
			String message = (String) result.get("message");

			if (isSuccess) {
				log.info("쿠폰 발급 API 성공: {}", message);
				return ResponseEntity.ok(Map.of("message", message));
			} else {
				// 서비스가 비즈니스 로직에 따라 실패를 반환한 경우 (예: 중복 발급, 조건 미충족)
				log.warn("쿠폰 발급 API 실패: {}", message);
				return ResponseEntity.badRequest().body(Map.of("message", message));
			}
		} catch (Exception e) {
			log.error("쿠폰 발급 API 처리 중 시스템 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
		}
	}
}
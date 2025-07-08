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

import jakarta.servlet.http.HttpSession; // HttpSession import 추가

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
			@RequestParam(value = "page", defaultValue = "1") int page, HttpSession session) { // 1. HttpSession 파라미터 추가

		// 1. 세션에서 'LoginUser' 객체를 가져옵니다.
		LoginUser loginUser = (LoginUser) session.getAttribute("LoginUser");

		// 2. 비로그인 사용자 예외 처리
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// 3. LoginUser 객체에서 사용자 번호를 가져옵니다.
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

		// ▼▼▼ 이 코드가 추가되어야 합니다 ▼▼▼

		// 1. 세션에서 'LoginUser'라는 이름으로 저장된 객체를 가져옵니다.
		LoginUser loginUser = (LoginUser) session.getAttribute("LoginUser");

		// 2. 만약 로그인 정보가 없으면(비로그인), 권한 없음 에러를 반환합니다.
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// 3. 가져온 객체에서 사용자 번호를 꺼내 userNo 변수에 담습니다.
		String userNo = loginUser.getUserNo();

		// ▲▲▲ 여기까지 추가 ▲▲▲

		log.info("API 호출 - 보유 쿠폰 조회: userNo={}, keyword={}, page={}", userNo, keyword, sortOrder, page);
		CustomerPagination<UserCoupons> myCouponsPage = couponsService.getMyCoupons(userNo, keyword, sortOrder, page);

		return new ResponseEntity<>(myCouponsPage, HttpStatus.OK);
	}

	/**
	 * 쿠폰 발급을 처리하는 API (최종 수정본)
	 */
	@PostMapping("/{couponId}/issue")
	public ResponseEntity<Map<String, String>> issueCoupon(@PathVariable String couponId, HttpSession session) { // 1.
																													// HttpSession
																													// 파라미터
																													// 추가

		// 2. 세션에서 로그인된 사용자 정보를 가져옵니다.
		LoginUser loginUser = (LoginUser) session.getAttribute("LoginUser");

		// 3. 비로그인 사용자 예외 처리
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 후 이용해주세요."));
		}

		// 4. LoginUser 객체에서 사용자 번호를 가져옵니다.
		String userNo = loginUser.getUserNo();

		log.info("API 호출 - 쿠폰 발급: userNo={}, couponId={}", userNo, couponId);

		try {
			// 5. 실제 userNo를 서비스에 전달합니다.
			boolean isSuccess = couponsService.issueCouponToUser(userNo, couponId);
			if (isSuccess) {
				return ResponseEntity.ok(Map.of("message", "쿠폰이 발급되었습니다."));
			} else {
				// 이 부분은 서비스 로직에서 예외(Exception)를 발생시켜 처리하는 것이 더 좋습니다.
				return ResponseEntity.badRequest().body(Map.of("message", "쿠폰 발급에 실패했거나, 이미 발급받은 쿠폰입니다."));
			}
		} catch (Exception e) {
			log.error("쿠폰 발급 중 오류 발생: {}", e.getMessage());
			// 서비스에서 보낸 구체적인 실패 사유를 사용자에게 전달합니다.
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}
}
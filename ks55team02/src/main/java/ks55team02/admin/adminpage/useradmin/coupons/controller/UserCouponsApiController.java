package ks55team02.admin.adminpage.useradmin.coupons.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import ks55team02.admin.adminpage.useradmin.coupons.service.UserCouponsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/adminpage/useradmin/api") // 기존 경로 사용 또는 신규 경로
@RequiredArgsConstructor
@Slf4j
public class UserCouponsApiController {

	private final UserCouponsService userCouponsService; // 새로운 서비스 주입

	@GetMapping("/user-coupons") // 새로운 API 주소
    public Map<String, Object> getUserCouponsListApi(@ModelAttribute AdminCouponsSearch search) {
        return userCouponsService.getUserCouponsList(search);
	}
        
        // [최종 수정] 쿠폰 회수 API를 PostMapping 방식으로 변경
	@PostMapping("/user-coupons/revoke/{userCpnId}")
	public ResponseEntity<?> revokeUserCoupon(@PathVariable String userCpnId) {
		try {
			// Service 메소드 호출 (이 부분은 그대로입니다)
			userCouponsService.updateUserCouponStatus(userCpnId);

			// 성공 시, success:true JSON 반환 (요청하신 구조)
			return ResponseEntity.ok(Map.of("success", true));

		} catch (Exception e) {
			log.error("쿠폰 회수 중 오류 발생: userCpnId={}", userCpnId, e);
			// 실패 시, 에러 메시지 포함한 JSON 반환 (요청하신 구조)
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 내부 오류가 발생했습니다."));
		}
	}

}
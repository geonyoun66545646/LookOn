package ks55team02.customer.coupons.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.service.CouponsService;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value="/customer")
@Slf4j
public class CouponsController {
	

	@GetMapping("/coupons")
    public String customerCoupons() {
        return "customer/coupons/coupons"; // templates/customer/coupons/coupons.html
    }
	
	// 2025.07.11 gy
	private final CouponsService couponsService;	
	
	// 2025.07.11 gy
    public CouponsController(CouponsService couponsService) {
        this.couponsService = couponsService;
    }
    
    // 2025.07.11 gy
    // 새로운 API 엔드포인트 추가
    // 현재 로그인 사용자의 쿠폰 조회 API
    @GetMapping("/api/user/coupons")
    @ResponseBody
    public ResponseEntity<List<UserCoupons>> getUserAvailableCoupons(HttpSession session) {
        // 1. 세션에서 사용자 ID 추출
        String userNo = (String) session.getAttribute("userNo");

        log.info("API 호출: /customer/api/user/coupons, 세션에 저장된 userNo: {}", userNo); // 디버깅 로그 추가

        // 2. 로그인 여부 확인
        if (userNo == null) {
            log.warn("쿠폰 API 호출 - userNo가 세션에 없어 401 UNAUTHORIZED 반환"); // 경고 로그 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        try {
            List<UserCoupons> coupons = couponsService.getUserAvailableCoupons(userNo);
            log.info("쿠폰 API 호출 성공 - userNo: {}, 반환된 쿠폰 수: {}", userNo, coupons.size()); // 성공 로그 추가
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            log.error("쿠폰 API 호출 중 서버 에러 발생 - userNo: {}", userNo, e); // 에러 로그 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

	
}

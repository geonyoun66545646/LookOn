package ks55team02.customer.coupons.controller;

import ks55team02.util.CustomerPagination;
import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.service.CouponsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(value = "page", defaultValue = "1") int page) {
        
        // TODO: 실제 로그인한 사용자 아이디를 가져오는 로직 필요 (스프링 시큐리티 연동)
        String userNo = "user_no_144"; // 임시 사용자 ID
        
        log.info("API 호출 - 발급 가능 쿠폰 조회: keyword={}, sort={}, page={}", keyword, sortOrder, page);
        CustomerPagination<Coupons> availableCouponsPage = couponsService.getAvailableCoupons(userNo, keyword, sortOrder, page);
        return new ResponseEntity<>(availableCouponsPage, HttpStatus.OK);
    }

    /**
     * '보유 쿠폰' 탭의 쿠폰 목록을 조회하는 API
     */
    @GetMapping("/my")
    public ResponseEntity<CustomerPagination<UserCoupons>> getMyCoupons(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "recent") String sortOrder,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        // TODO: 실제 로그인한 사용자 아이디를 가져오는 로직 필요 (스프링 시큐리티 연동)
        String userNo = "user_no_144"; // 임시 사용자 ID

        log.info("API 호출 - 보유 쿠폰 조회: keyword={}, sort={}, page={}", keyword, sortOrder, page);
        CustomerPagination<UserCoupons> myCouponsPage = couponsService.getMyCoupons(userNo, keyword, sortOrder, page);
        return new ResponseEntity<>(myCouponsPage, HttpStatus.OK);
    }

    /**
     * 쿠폰 발급을 처리하는 API
     */
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Map<String, String>> issueCoupon(@PathVariable String couponId) {
        
        // TODO: 실제 로그인한 사용자 아이디를 가져오는 로직 필요 (스프링 시큐리티 연동)
        String userNo = "user_no_144"; // 임시 사용자 ID

        log.info("API 호출 - 쿠폰 발급: userNo={}, couponId={}", userNo, couponId);
        
        try {
            boolean isSuccess = couponsService.issueCouponToUser(userNo, couponId);
            if (isSuccess) {
                return ResponseEntity.ok(Map.of("message", "쿠폰이 발급되었습니다."));
            } else {
                // 서비스에서 발급 실패 사유를 반환하도록 수정할 수 있습니다.
                return ResponseEntity.badRequest().body(Map.of("message", "쿠폰 발급에 실패했거나, 이미 발급받은 쿠폰입니다."));
            }
        } catch (Exception e) {
            log.error("쿠폰 발급 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "오류가 발생했습니다."));
        }
    }
}
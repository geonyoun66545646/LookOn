package ks55team02.admin.adminpage.useradmin.coupons.controller;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adminpage/useradmin/api")
@RequiredArgsConstructor
@Slf4j
public class AdminCouponsApiController {

    private final AdminCouponsService adminCouponsService;

    @GetMapping("/coupons")
    public Map<String, Object> getCouponsListApi(@ModelAttribute AdminCouponsSearch search) {
        log.info("API 수신 검색 조건: {}", search);
        return adminCouponsService.getCouponsList(search);
    }

    @PostMapping("/coupons/batch-delete")
    public ResponseEntity<?> batchDeleteCoupons(@RequestBody List<String> pblcnCpnIdList) {
        try {
            adminCouponsService.batchDeleteCoupons(pblcnCpnIdList);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("쿠폰 일괄 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 내부 오류가 발생했습니다."));
        }
    }
    
    // [신규] 선택 활성화 API 메소드 추가
    @PostMapping("/coupons/batch-activate")
    public ResponseEntity<?> batchActivateCoupons(@RequestBody List<String> pblcnCpnIdList) {
        try {
            adminCouponsService.batchActivateCoupons(pblcnCpnIdList);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("쿠폰 일괄 활성화 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 내부 오류가 발생했습니다."));
        }
    }
}
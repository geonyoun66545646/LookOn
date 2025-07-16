package ks55team02.admin.adminpage.useradmin.coupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class UserCouponsController {

    /**
     * 회원 쿠폰 목록 조회 페이지로 이동
     */
    @GetMapping("/user-coupons") // 새로운 페이지 주소
    public String userCouponsListPage() {
        // userCouponsList.html 파일을 반환
        return "admin/adminpage/useradmin/coupons/userCouponsList";
    }

    // ==========================================================
    // 여기에 있었을 쿠폰 생성/수정 관련 @GetMapping, @PostMapping 등
    // 모든 메소드를 삭제합니다.
    // ==========================================================
}
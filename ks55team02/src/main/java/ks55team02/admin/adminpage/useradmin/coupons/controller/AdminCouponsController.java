package ks55team02.admin.adminpage.useradmin.coupons.controller;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
public class AdminCouponsController {

    private final AdminCouponsService adminCouponsService;

    /**
     * 쿠폰 목록 조회 페이지
     */
    @GetMapping("/couponsList")
    public String getCouponsList(Model model) {
        
        // 1. 서비스에 쿠폰 목록을 요청합니다.
        List<AdminCoupons> couponsList = adminCouponsService.getCouponsList();
        
        // 2. 받아온 목록을 모델에 담아 View로 전달합니다.
        model.addAttribute("couponsList", couponsList);
        model.addAttribute("title", "쿠폰 목록/관리");
        
        return "admin/adminpage/useradmin/couponsList";
    }

    /**
     * 신규 쿠폰 생성 페이지
     */
    @GetMapping("/createCoupons")
    public String getCreateCoupons(Model model) {
        model.addAttribute("title", "신규 쿠폰 생성");
        return "admin/adminpage/useradmin/createCoupons";
    }

    /**
     * 쿠폰 수정 페이지
     */
    @GetMapping("/editCoupons")
    public String getEditCoupons(Model model) {
        model.addAttribute("title", "쿠폰 정보 수정");
        return "admin/adminpage/useradmin/editCoupons";
    }

    /**
     * 회원별 쿠폰 관리 페이지
     */
    @GetMapping("/memberCoupons")
    public String getMemberCoupons(Model model) {
        model.addAttribute("title", "회원별 쿠폰 관리");
        return "admin/adminpage/useradmin/memberCoupons";
    }
}
package ks55team02.admin.adminpage.useradmin.coupons.controller;

// @ResponseBody 어노테이션 임포트 및 사용 제거
// getCouponsData 메소드 제거

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
public class AdminCouponsController {
    private final AdminCouponsService adminCouponsService;

    // 기존 뷰 페이지 로딩 메소드 (변경 없음)
    @GetMapping("/couponsList")
    public String couponsListPage(@ModelAttribute AdminCouponsSearch adminCouponsSearch, Model model) {
        model.addAttribute("title", "쿠폰 목록/관리");
        return "admin/adminpage/useradmin/couponsList";
    }

    // --- 이하 다른 웹 페이지 관련 메소드들은 유지 ---

    // [추가] 쿠폰 삭제 처리 (GET 방식)
    @GetMapping("/deleteCoupons/{pblcnCpnId}")
    public String deleteCoupon(@PathVariable("pblcnCpnId") String pblcnCpnId) {
        adminCouponsService.deleteCoupon(pblcnCpnId);
        return "redirect:/adminpage/useradmin/couponsList";
    }

    // 신규 쿠폰 생성 페이지
    @GetMapping("/createCoupons")
    public String getCreateCoupons(Model model) {
        model.addAttribute("title", "신규 쿠폰 생성");
        return "admin/adminpage/useradmin/createCoupons";
    }

    // 신규 쿠폰 정보 DB에 저장 (POST 방식)
    @PostMapping("/createCoupons")
    public String addCoupon(AdminCoupons adminCoupons) {
        adminCouponsService.addCoupon(adminCoupons);
        return "redirect:/adminpage/useradmin/couponsList";
    }

    // (수정) 쿠폰 수정 페이지로 이동 + null 체크 추가
    @GetMapping("/editCoupons/{pblcnCpnId}")
    public String getEditCoupons(@PathVariable("pblcnCpnId") String pblcnCpnId, Model model,
            RedirectAttributes redirectAttributes) {
        AdminCoupons couponInfo = adminCouponsService.getCouponById(pblcnCpnId);
        if (couponInfo == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 쿠폰 정보입니다.");
            return "redirect:/adminpage/useradmin/couponsList";
        }
        model.addAttribute("title", "쿠폰 정보 수정");
        model.addAttribute("couponInfo", couponInfo);
        return "admin/adminpage/useradmin/editCoupons";
    }

    // 쿠폰 수정 내용 처리 (POST 방식)
    @PostMapping("/editCoupons")
    public String updateCoupon(AdminCoupons adminCoupons) {
        adminCouponsService.updateCoupon(adminCoupons);
        return "redirect:/adminpage/useradmin/couponsList";
    }

    // 회원별 쿠폰 관리 페이지
    @GetMapping("/memberCoupons")
    public String getMemberCoupons(Model model) {
        model.addAttribute("title", "회원별 쿠폰 관리");
        return "admin/adminpage/useradmin/memberCoupons";
    }
}
package ks55team02.admin.adminpage.useradmin.coupons.controller;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
public class AdminCouponsController {

	private final AdminCouponsService adminCouponsService;
	
	 /**
     * ======================================================
     * [추가] 쿠폰 삭제 처리 (GET 방식)
     * ======================================================
     */
    @GetMapping("/deleteCoupons/{pblcnCpnId}")
    public String deleteCoupon(@PathVariable("pblcnCpnId") String pblcnCpnId) {
        
        adminCouponsService.deleteCoupon(pblcnCpnId);
        
        // 삭제가 끝나면 쿠폰 목록 페이지로 리다이렉트
        return "redirect:/adminpage/useradmin/couponsList";
    }

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
	 * ====================================================== [추가/확인] 신규 쿠폰 정보 DB에
	 * 저장 (POST 방식) ======================================================
	 */
	@PostMapping("/createCoupons")
	public String addCoupon(AdminCoupons adminCoupons) {

		adminCouponsService.addCoupon(adminCoupons);

		// 저장이 끝나면 쿠폰 목록 페이지로 리다이렉트
		return "redirect:/adminpage/useradmin/couponsList";
	}

	/**
	 * (수정) 쿠폰 수정 페이지로 이동 + null 체크 추가
	 */
	@GetMapping("/editCoupons/{pblcnCpnId}")
	public String getEditCoupons(@PathVariable("pblcnCpnId") String pblcnCpnId, Model model,
			RedirectAttributes redirectAttributes) { // RedirectAttributes 추가

		// 1. 서비스에 ID를 전달하여 해당 쿠폰의 전체 정보를 가져옵니다.
		AdminCoupons couponInfo = adminCouponsService.getCouponById(pblcnCpnId);

		// 2. [핵심] 가져온 정보가 null인지 확인합니다.
		if (couponInfo == null) {
			// 정보가 없으면, 리다이렉트 페이지에 메시지를 담아 보냅니다.
			redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 쿠폰 정보입니다.");
			return "redirect:/adminpage/useradmin/couponsList"; // 목록 페이지로 리다이렉트
		}

		// 3. 정보가 있다면, 정상적으로 모델에 담아 수정 페이지로 보냅니다.
		model.addAttribute("title", "쿠폰 정보 수정");
		model.addAttribute("couponInfo", couponInfo);

		return "admin/adminpage/useradmin/editCoupons";
	}

	/**
	 * ====================================================== [추가] 쿠폰 수정 내용 처리 (POST
	 * 방식) ======================================================
	 */
	@PostMapping("/editCoupons")
	public String updateCoupon(AdminCoupons adminCoupons) {

		adminCouponsService.updateCoupon(adminCoupons);

		// 수정이 끝나면 쿠폰 목록 페이지로 리다이렉트
		return "redirect:/adminpage/useradmin/couponsList";
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
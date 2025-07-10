package ks55team02.admin.adminpage.useradmin.couponslist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class CouponsListController {

	// 채팅 로그
	@GetMapping("/couponsList")
	public String getAdminPageCouponsList() {
				
	return "admin/adminpage/useradmin/couponsList";
	}
}
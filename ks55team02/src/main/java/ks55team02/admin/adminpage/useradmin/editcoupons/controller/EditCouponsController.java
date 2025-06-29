package ks55team02.admin.adminpage.useradmin.editcoupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class EditCouponsController {

	// 쿠폰 변경
	@GetMapping("/editCoupons")
	public String useradminEditCouponsController() {
		
		return "admin/adminpage/useradmin/editCoupons";
	}
}

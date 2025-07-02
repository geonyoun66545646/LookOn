package ks55team02.admin.adminpage.useradmin.createcoupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class CreateCouponsController {

	// 쿠폰 생성
	@GetMapping("/createCoupons")
	public String useradminCreateCouponsController() {
		
	return "admin/adminpage/useradmin/createCoupons";
	}
}

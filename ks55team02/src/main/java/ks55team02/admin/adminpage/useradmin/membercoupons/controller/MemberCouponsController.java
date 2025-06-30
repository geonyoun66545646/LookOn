package ks55team02.admin.adminpage.useradmin.membercoupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class MemberCouponsController {

	@GetMapping("/memberCoupons")
	public String useradminMemberCouponsController() {
		
		return "admin/adminpage/useradmin/memberCoupons";
	}
}

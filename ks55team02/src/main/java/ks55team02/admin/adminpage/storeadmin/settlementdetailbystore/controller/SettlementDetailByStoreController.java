package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/storeadmin")
public class SettlementDetailByStoreController {

	@GetMapping("/settlementDetailByStore")
	public String storeadminsettlementController() {
		
		return "admin/adminpage/storeadmin/settlementDetailByStore";
	}
}

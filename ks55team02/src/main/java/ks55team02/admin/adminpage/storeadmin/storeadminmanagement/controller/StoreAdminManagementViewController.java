package ks55team02.admin.adminpage.storeadmin.storeadminmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/storeadmin")
public class StoreAdminManagementViewController {

	@GetMapping("/storeAdminManagementView")
	public String storeadminManagementController() {
		
		return "admin/adminpage/storeadmin/storeAdminManagementView";
	}
}

package ks55team02.admin.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreAdminManagementController {
	
	@GetMapping("/inqueryAdmin")
	public String inqueryAdminView() {
		
		return "admin/store/inqueryAdminView";
	}
	@GetMapping("/storeAdminManagement")
	public String storeAdminManagementView() {
		
		return "admin/store/storeAdminManagementView";
	}
	@GetMapping("/storeApllicationManagement")
	public String storeApllicationManagementView() {
		
		return "admin/store/storeApllicationManagementView";
	}

}

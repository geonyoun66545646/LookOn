package ks55team02.admin.adminpage.storeadmin.storeapllicationmanagementview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/storeadmin")
public class StoreApllicationManagementViewController {

	@GetMapping("/storeApllicationManagementView")
	public String storeadminApllicationController() {
		
		return "admin/adminpage/storeadmin/storeApllicationManagementView";
	}
}

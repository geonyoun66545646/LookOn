package ks55team02.admin.adminpage.productadmin.adminproductmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/productadmin")
public class AdminCategoryManagementController {

	@GetMapping("/adminCategoryManagement")
	public String productadminCategoryController() {
		
		return "admin/adminpage/productadmin/adminCategoryManagement";
	}
}

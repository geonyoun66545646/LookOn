package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/productadmin")
public class AdminProductManagementController {

	@GetMapping("/adminProductManagement")
	public String productadminController() {
		
		return "admin/adminpage/productadmin/adminProductManagement";
	}
}

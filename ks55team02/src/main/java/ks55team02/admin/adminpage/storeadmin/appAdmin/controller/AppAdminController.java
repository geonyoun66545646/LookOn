package ks55team02.admin.adminpage.storeadmin.appAdmin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;
import ks55team02.admin.adminpage.storeadmin.appAdmin.service.AppAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
@Log4j2
public class AppAdminController {
	
	private final AppAdminService appAdminService;

	@GetMapping("/appAdmin")
	public String appAdminView(Model model) {
		
		
	List<AppAdmin> appAdminList = appAdminService.getAppAdminList(); 
	log.info("AppAdminList에 담긴 데이터 개수: {}", appAdminList.size());
	
		
		model.addAttribute("title", "상점 신청");
		model.addAttribute("appAdminList", appAdminList);
		
		return "admin/adminpage/storeadmin/appAdminView";
	}
	
	@GetMapping("/appDetail")
	public String appDeailView(Model model) {
		model.addAttribute("title", "상점 신청 상세");
		
		return "admin/adminpage/storeadmin/appDetailView";
	}
	
}

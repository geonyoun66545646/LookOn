package ks55team02.admin.adminpage.storeadmin.appAdmin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
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
	public String appAdmin(Model model) {
		
		
	List<AppAdmin> appAdminList = appAdminService.getAppAdminList(); 
		model.addAttribute("title", "상점 신청");
		// 상점 신청(어드민페이지) 전체 조회
		model.addAttribute("appAdminList", appAdminList);
		
		return "admin/adminpage/storeadmin/appAdminView";
	}
	
	@GetMapping("/appDetail")
	public String getAppDetail(@RequestParam("aplyId") String aplyId, Model model) {
	    log.info("컨트롤러: getAppDetail 호출 - aplyId: {}", aplyId);

	    AppAdmin appAdmin = appAdminService.getAppAdminById(aplyId);

	    log.info("컨트롤러: appAdminService.getAppAdminById 결과: {}", appAdmin);

	    if (appAdmin != null) {
	        model.addAttribute("title", "상점 신청 상세");
	        model.addAttribute("appAdmin", appAdmin);
	        log.info("컨트롤러: model에 appAdmin 추가 완료");
	        return "admin/adminpage/storeadmin/appDetailView"; // 데이터가 있을 때만 상세 뷰로
	    } else {
	        log.warn("해당 aplyId로 조회된 AppAdmin 데이터가 없습니다: {}", aplyId);
	        model.addAttribute("errorMessage", "신청 정보를 찾을 수 없습니다.");
	        return "error/dataNotFound"; // 데이터가 없을 때 에러 뷰로 리다이렉트
	    }
	}
	
	@GetMapping("/summer")
	public String summer() {
		
		return "admin/adminpage/storeadmin/sumer";
	}
	
	
}

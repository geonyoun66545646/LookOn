package ks55team02.customer.reports.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/customer")
public class ReportsController {

	@GetMapping("/reports")
	// 1. URL로부터 파라미터를 받을 수 있도록 @RequestParam 어노테이션을 추가합니다.
//	    required = false는 해당 파라미터가 URL에 없어도 에러를 발생시키지 않도록 합니다.
	public String customerReports(@RequestParam(required = false) String targetType,
			@RequestParam(required = false) String targetId, @RequestParam(required = false) String targetName,
			Model model) { // 2. 뷰(HTML)에 데이터를 전달하기 위해 Model 객체를 추가합니다.

		// 3. 받은 파라미터를 "reportInfo"라는 이름의 Map에 담아 정리합니다.
		// 이렇게 하면 HTML에서 ${reportInfo.targetType} 처럼 접근하기 편리해집니다.
		Map<String, String> reportInfo = new HashMap<>();
		reportInfo.put("targetType", targetType);
		reportInfo.put("targetId", targetId);
		reportInfo.put("targetName", targetName);

		// 4. 정리된 정보를 Model에 담아 뷰로 전달합니다.
		model.addAttribute("reportInfo", reportInfo);

		return "customer/reports/reports";
	}

	@GetMapping("/myReports")
	public String customerMyReports() {
		return "customer/reports/myReports"; // templates/customer/reports/myReports.html
	}

}
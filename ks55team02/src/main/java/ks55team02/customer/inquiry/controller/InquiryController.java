package ks55team02.customer.inquiry.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.customer.inquiry.domain.Inquiry;
import ks55team02.customer.inquiry.domain.InquiryOption;
import ks55team02.customer.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequestMapping("/customer/inquiry")
@Controller
@RequiredArgsConstructor
@Log4j2
public class InquiryController {
	
	 private final InquiryService inquiryService;
	 
	// 자주묻는 질문 페이지
	@GetMapping("/frequentlyAskedQuestions")
	public String frequentlyAskedQuestionsView() {
		
		return "customer/inquiry/frequentlyAskedQuestionsView";
	}
	
	// 문의 상세
	@GetMapping("/inquiryDetail")
	public String inquiryView(Model model) {
		
		List<Inquiry> inquiryDetail = inquiryService.getInquiryDetail();
		
		model.addAttribute("title","특정 문의 조회");
		model.addAttribute("inquiryDetail", inquiryDetail);

		return "customer/inquiry/inquiryDetailView";
	}
	// 문의 등록 & 문의 옵션 
	@GetMapping("/addInquiry")
	public String addInquiryView(Model model) {
        model.addAttribute("title", "문의 등록");
        model.addAttribute("inquiry", new Inquiry());
        model.addAttribute("inquiryType", InquiryOption.values());
        
        return "customer/inquiry/addInquiryView";
    }

		// 문의 등록 POST 요청
		// "/addInquiry" 폼의 th:action 과 일치
	@PostMapping("/addInquiry")
	public ResponseEntity<Map<String, Object>> addInquiry(Inquiry inquiry) {
		log.info("AJAX 요청 수신된 문의 정보 (전체): {}", inquiry);
		log.info("AJAX 요청 수신된 문의 prvtYn 값: {}", inquiry.isPrvtYn());
	    log.info("AJAX 요청 수신된 문의 inqryTypeCd 값: {}", inquiry.getInqryTypeCd());
	    
	    Map<String, Object> response = new HashMap<>();
	    try {
	    	inquiryService.addInquiry(inquiry);
	    	
	    	response.put("status", "success");
	    	response.put("message", "등록 성공");
	    	return ResponseEntity.ok(response);
	    } catch (Exception e) {
	    	 log.error("문의 등록 실패 (AJAX): {}", e.getMessage(), e); // 에러 로그
	            response.put("status", "error");
	            response.put("message", "문의 등록 중 오류가 발생했습니다: " + e.getMessage());
	            return ResponseEntity.status(500).body(response);       
	    }
	}
	 // 문의 목록 조회
    @GetMapping("/inquiryList")
    public String getInquiryList(Model model) {
        List<Inquiry> inquiryList = inquiryService.getInquiryList();
        model.addAttribute("title", "문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        return "customer/inquiry/inquiryListView";
    }
		
		
		
	
}

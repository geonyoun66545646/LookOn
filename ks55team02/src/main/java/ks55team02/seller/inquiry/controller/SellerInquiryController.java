package ks55team02.seller.inquiry.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@Log4j2
public class SellerInquiryController {
	
	private final SellerInquiryService sellerInquiryService;
	
	
	private String getCurrentSellerStoreId(HttpSession session) {
        log.info("[임시] 현재 로그인된 판매자 상점 ID: store_1");
        return "store_22";
	}
	
	//스토어 인쿼리
    @GetMapping("/sellerInquiryList")
    public String getSellerInquiryList(Model model, HttpSession session) {
    	// 로그인된 스토어 아이디를 가져옴
    	 String currentStoreId = getCurrentSellerStoreId(session);
    	 if (currentStoreId == null) {
             // 예시: 로그인되지 않은 판매자 처리
             // return "redirect:/seller/login";
        }
    	 
    	 List<Inquiry> inquiryList = sellerInquiryService.getSellerInquiryList(currentStoreId);
    	 
    	 // 3. Model에 데이터를 담아 뷰로 전달합니다.
         model.addAttribute("title", "판매자 문의 목록");
         model.addAttribute("inquiryList", inquiryList);
         model.addAttribute("currentStoreId", currentStoreId);
    	 
         model.addAttribute("totalRows", inquiryList.size());
         model.addAttribute("currentPage", 1);
    	 
    	
    	
    	return "seller/inquiry/sellerInquiryList";
    }

}

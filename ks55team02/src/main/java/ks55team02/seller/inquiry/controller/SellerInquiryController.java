package ks55team02.seller.inquiry.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// import jakarta.servlet.http.HttpSession; // HttpSession은 더 이상 직접 사용하지 않으므로 주석 처리 또는 제거 가능
import ks55team02.admin.common.domain.Pagination;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/seller/inquiry")
@RequiredArgsConstructor
@Log4j2
public class SellerInquiryController {
	
	private final SellerInquiryService sellerInquiryService;
	
	@GetMapping("/sellerInquiryList")
	public String sellerInquiryList(
	        Model model,
	        Inquiry inquiry
	        // HttpSession session // 세션에서 ID를 가져오지 않으므로 주석 처리 또는 제거
	) {
		log.info("컨트롤러: sellerInquiryList 호출 - 현재 페이지: {}, 페이지 크기: {}, 검색 키: {}, 검색 값: {}",
	             inquiry.getCurrentPage(), inquiry.getPageSize(), inquiry.getSearchKey(), inquiry.getSearchValue());

		// TODO: 실제 로그인된 상점 ID를 HTTP 세션에서 가져오는 로직 (하드코딩으로 변경)
		// String loggedInStoreId = (String) session.getAttribute("SID"); // 세션에서 가져오는 코드 주석 처리
		
		// ⭐ 현재 로그인된 상점 ID를 하드코딩합니다.
		String loggedInStoreId = "store_21"; // <-- 여기에 사용할 상점 ID를 입력하세요!
		
		// if (loggedInStoreId == null) { // 세션에 로그인된 ID가 없을 경우의 처리 로직은 하드코딩 시 필요 없습니다.
		// 	log.warn("세션에 로그인된 상점 ID(SID)가 없습니다. 로그인 페이지로 리다이렉트 또는 에러 처리 필요.");
		// 	// 예시: 로그인 페이지로 리다이렉트
		// 	return "redirect:/login";
		// }
		
		inquiry.setInqryStoreId(loggedInStoreId); // 하드코딩된 ID를 문의 객체에 설정
		log.info("로그인된 상점 ID (하드코딩): {}", loggedInStoreId); // 로그 메시지 변경

		// 1. 전체 개수 조회
		// 현재 문의 객체(inquiry)에 하드코딩된 loggedInStoreId가 설정되어 있으므로,
		// 해당 상점의 문의만 개수를 세게 됩니다.
		int totalRecordCount = sellerInquiryService.getSellerInquiryCnt(inquiry); 
		log.info("컨트롤러: 문의 전체 개수: {}", totalRecordCount);

		// 2. Pagination 객체 생성 및 페이지네이션 정보 계산
		Pagination pagination = new Pagination(totalRecordCount, inquiry);
		log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
	             pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());
		
		// 3. 목록 조회 시 LIMIT, OFFSET 값 설정을 위해 Inquiry 객체에 pagination 정보 주입
		inquiry.setOffset(pagination.getLimitStart());
		
		// 4. 판매자 문의 목록 조회 (페이지네이션 및 검색 조건 적용)
		// 마찬가지로, inquiry 객체에 설정된 상점 ID를 기반으로 해당 상점의 문의 목록만 조회됩니다.
		List<Inquiry> sellerInquiryList = sellerInquiryService.getSellerInquiryList(inquiry, pagination.getLimitStart(), inquiry.getPageSize());
		log.info("컨트롤러: sellerInquiryList 조회 결과 개수: {}", sellerInquiryList.size());
		
		// Model에 필요한 데이터 추가
		model.addAttribute("title", "판매자 문의 목록");
		model.addAttribute("sellerInquiryList", sellerInquiryList);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", inquiry); // 검색 조건 유지를 위해 inquiry 객체 전달
		
		return "seller/inquiry/sellerInquiryListView"; // 뷰 경로 반환
	}

	@GetMapping("/sellerInquiryDetail")
	public String getSellerInquiryDetail(@RequestParam("inqryId") String inqryId, Model model) {
	    log.info("컨트롤러: getSellerInquiryDetail 호출 - inqryId: {}", inqryId);

	    // 이 메서드는 특정 문의 ID로 상세 정보를 조회하므로, 상점 ID 하드코딩과는 직접적인 연관이 없습니다.
	    Inquiry inquiryDetail = sellerInquiryService.getSellerInquiryByStoreId(inqryId);

	    log.info("컨트롤러: sellerInquiryService.getSellerInquiryByStoreId 결과: {}", inquiryDetail);

	    if (inquiryDetail != null) {
	        model.addAttribute("title", "판매자 문의 상세");
	        model.addAttribute("inquiryDetail", inquiryDetail);
	        log.info("컨트롤러: model에 inquiryDetail 추가 완료");
	        return "seller/inquiry/sellerInquiryDetailView";
	    } else {
	        log.warn("해당 inqryId로 조회된 Inquiry 데이터가 없습니다: {}", inqryId);
	        model.addAttribute("errorMessage", "문의 정보를 찾을 수 없습니다.");
	        return "error/dataNotFound";
	    }
	}
}
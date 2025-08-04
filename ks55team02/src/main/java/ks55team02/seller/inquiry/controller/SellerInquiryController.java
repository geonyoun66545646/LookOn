package ks55team02.seller.inquiry.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // RequestParam 대신 RequestBody 임포트
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.customer.login.domain.LoginUser;
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
	        Inquiry inquiry,
	        @RequestParam(value = "filterConditions", required = false) String filterConditionsString,
	        HttpSession session
	) {
		log.info("컨트롤러: sellerInquiryList 호출 - 현재 페이지: {}, 페이지 크기: {}, 검색 키: {}, 검색 값: {}",
	             inquiry.getCurrentPage(), inquiry.getPageSize(), inquiry.getSearchKey(), inquiry.getSearchValue());
		log.info("컨트롤러: filterConditionsString (Raw): {}", filterConditionsString);

		if (filterConditionsString != null && !filterConditionsString.trim().isEmpty()) {
		    List<String> filterConditions = Arrays.asList(filterConditionsString.split(","));
		    inquiry.setFilterConditions(filterConditions);
		    log.info("컨트롤러: 파싱된 filterConditions: {}", filterConditions);
		} else {
		    inquiry.setFilterConditions(null);
		}

		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		String userLoginId = null;
		String storeId = null;

		if (loginUser != null) {
		    userLoginId = loginUser.getUserLgnId();
		    log.info("세션에서 가져온 로그인 사용자 ID: {}", userLoginId);
		    storeId = sellerInquiryService.getStoreIdByUserLgnId(userLoginId);
		    log.info("로그인 사용자 ID({})에 해당하는 상점 ID: {}", userLoginId, storeId);
		} else {
		    log.warn("세션에 로그인 정보가 없습니다.");
		}
		
		/* 기간 기본 값. */
        // 1. 시작 날짜가 비어있는지(null) 확인합니다.
        if (inquiry.getStartDate() == null) {
        	inquiry.setStartDate(LocalDate.parse("2020-01-01"));
        }

        // 2. 종료 날짜가 비어있는지(null) 확인합니다.
        if (inquiry.getEndDate() == null) {
        	inquiry.setEndDate(LocalDate.now());
        }
        inquiry.setEndDate(inquiry.getEndDate().plusDays(1));
        
		inquiry.setInqryStoreId(storeId);

		int totalRecordCount = sellerInquiryService.getSellerInquiryCnt(inquiry);
		log.info("컨트롤러: 문의 전체 개수: {}", totalRecordCount);

		Pagination pagination = new Pagination(totalRecordCount, inquiry);
		log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
	             pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());
		
		inquiry.setOffset(pagination.getLimitStart());
		
		List<Inquiry> sellerInquiryList = sellerInquiryService.getSellerInquiryList(inquiry, pagination.getLimitStart(), inquiry.getPageSize());
		log.info("컨트롤러: sellerInquiryList 조회 결과 개수: {}", sellerInquiryList.size());
		
		model.addAttribute("title", "판매자 문의 목록");
		model.addAttribute("sellerInquiryList", sellerInquiryList);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", inquiry);
		
		return "seller/inquiry/sellerInquiryListView";
	}

	@GetMapping("/sellerInquiryDetail")
	public String getSellerInquiryDetail(@RequestParam("inqryId") String inqryId, Model model) {
	    log.info("컨트롤러: getSellerInquiryDetail 호출 - inqryId: {}", inqryId);

		
		  Inquiry inquiryDetail = sellerInquiryService.getSellerInquiryByStoreId(inqryId);
		 

	    log.info("컨트롤러: sellerInquiryService.getSellerInquiryByStoreId 결과: {}", inquiryDetail);
	    
	    // ★★★ API URL을 모델에 담아 전달합니다. ★★★
	    model.addAttribute("answerApiUrl", "/seller/inquiry/answerProcess");
	    
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
	
	// ★★★★★ @RequestParam 방식에서 @RequestBody 방식으로 수정된 메소드 ★★★★★
	@PostMapping("/answerProcess")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> processAnswer(
			@RequestBody Map<String, String> payload,
			HttpSession session
			) {
		log.info("컨트롤러: AJAX JSON 답변 요청 수신 - payload: {}", payload);
		Map<String, Object> response = new HashMap<>();
		
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		if (loginUser == null) {
		    log.error("컨트롤러: 로그인된 사용자 정보가 세션에 없습니다.");
		    response.put("status", "error");
		    response.put("message", "로그인 정보가 없어 답변 처리를 할 수 없습니다.");
		    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		
		String userLoginId = loginUser.getUserLgnId();
		String storeId = sellerInquiryService.getStoreIdByUserLgnId(userLoginId);
		String answrUserNo = sellerInquiryService.getSellerUserNoByStoreId(storeId);
		
		if (answrUserNo == null || answrUserNo.isEmpty()) {
		    log.error("컨트롤러: 상점 ID({})에 해당하는 판매자 사용자 번호를 찾을 수 없습니다.", storeId);
		    response.put("status", "error");
		    response.put("message", "답변자 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		
		String inqryId = payload.get("inqryId");
		String ansId = payload.get("ansId");
		String ansCn = payload.get("ansCn");
		
		log.info("컨트롤러: 답변자 사용자 번호: {}", answrUserNo);

		try {
			Answer processedAnswer;
			if (ansId == null || ansId.isEmpty()) {
				log.info("컨트롤러: 신규 답변 등록 로직 실행");
				processedAnswer = sellerInquiryService.registerAnswer(inqryId, ansCn, answrUserNo);
				response.put("message", "답변이 성공적으로 등록되었습니다.");
			} else {
				log.info("컨트롤러: 기존 답변 수정 로직 실행 - 대상 ansId: {}", ansId);
				processedAnswer = sellerInquiryService.updateAnswer(ansId, inqryId, ansCn, answrUserNo);
				response.put("message", "답변이 성공적으로 수정되었습니다.");
			}
			response.put("status", "success");
			
			if (processedAnswer != null) {
				response.put("ansId", processedAnswer.getAnsId());
				response.put("ansCn", processedAnswer.getAnsCn());
				response.put("answrUserNo", processedAnswer.getAnswrUserNo());
				response.put("ansTm", processedAnswer.getAnsTm() != null ? processedAnswer.getAnsTm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
				response.put("lastMdfcnDt", processedAnswer.getLastMdfcnDt() != null ? processedAnswer.getLastMdfcnDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
				log.info("컨트롤러: 답변 처리 성공 - 응답 데이터: {}", response);
			} else {
			    log.warn("컨트롤러: 답변 처리 성공했으나, 반환된 Answer 객체가 null입니다.");
			}
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("컨트롤러: 답변 처리 중 오류 발생 - inqryId: {}, ansId: {}", inqryId, ansId, e);
			response.put("status", "error");
			response.put("message", "답변 처리 실패: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
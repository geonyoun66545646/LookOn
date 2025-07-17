package ks55team02.admin.inquiry.controller;

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
import org.springframework.web.bind.annotation.ModelAttribute; // @ModelAttribute 임포트 추가
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;

import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.inquiry.service.AdminInquiryService;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Log4j2
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    // 관리자 문의 목록 조회
    @GetMapping("/adminInquiryList")
    public String adminInquiryList(
            Model model,
            // 이전 @RequestParam들은 @ModelAttribute Inquiry searchInquiry 로 대체됩니다.
            // filterConditionsString 은 List<String>으로 자동 바인딩되지 않으므로 유지합니다.
            @RequestParam(name = "filterConditions", required = false) String filterConditionsString,
            @ModelAttribute Inquiry searchInquiry // ⭐ 모든 검색 조건이 Inquiry 객체에 자동으로 바인딩됩니다.
    ) {
        log.info("관리자 문의 목록 요청 - currentPage: {}, pageSize: {}", searchInquiry.getCurrentPage(), searchInquiry.getPageSize());
        log.info("검색 조건: searchKey={}, searchValue={}, startDate={}, endDate={}, filterConditions={}, sortKey={}, sortOrder={}, inqryTrgtTypeCd={}",
                 searchInquiry.getSearchKey(), searchInquiry.getSearchValue(), searchInquiry.getStartDate(), searchInquiry.getEndDate(),
                 searchInquiry.getFilterConditions(), searchInquiry.getSortKey(), searchInquiry.getSortOrder(), searchInquiry.getInqryTrgtTypeCd());

        // filterConditionsString을 List<String>으로 변환하여 searchInquiry에 설정
        if (filterConditionsString != null && !filterConditionsString.isEmpty()) {
            List<String> filterConditions = Arrays.asList(filterConditionsString.split(","));
            searchInquiry.setFilterConditions(filterConditions);
            log.info("변환된 필터 조건: {}", filterConditions);
        } else {
            searchInquiry.setFilterConditions(null);
        }

        // 1. 전체 문의 개수 조회
        int totalInquiryCnt = adminInquiryService.getAdminInquiryCnt(searchInquiry);
        log.info("총 문의 개수: {}", totalInquiryCnt);

        // 2. Pagination 객체 생성 (SearchCriteria를 상속받는 searchInquiry 객체를 전달)
        // searchInquiry는 Inquiry 타입이지만, Pagination 생성자가 SearchCriteria를 받으므로 호환됩니다.
        Pagination pagination = new Pagination(totalInquiryCnt, searchInquiry);
        log.info("페이지네이션 정보: {}", pagination);

        // 3. limitStart를 searchInquiry에 설정 (MyBatis 쿼리에서 사용)
        searchInquiry.setOffset(pagination.getLimitStart());
        
        // 4. 문의 목록 조회 (페이지네이션 및 검색 조건 적용)
        List<Inquiry> inquiryList = adminInquiryService.getAdminInquiryList(searchInquiry, pagination.getLimitStart(), pagination.getRecordSize());
        log.info("조회된 문의 목록: {}", inquiryList);

        // 문의 처리 상태 목록을 모델에 추가하여 Thymeleaf 템플릿에서 직접 접근 가능하도록 함
        List<String> processStatuses = Arrays.asList("접수", "완료", "진행중");
        model.addAttribute("processStatuses", processStatuses);

        model.addAttribute("title", "관리자 문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchInquiry); // 현재 검색 조건을 뷰에 전달

        // filterConditionsString은 @RequestParam으로 받기 때문에 여전히 모델에 추가합니다.
        // 다른 검색 조건들은 searchInquiry 객체 내에 있으므로 별도 추가가 필요 없습니다.
        model.addAttribute("filterConditionsString", filterConditionsString); 
        
        // inqryTrgtTypeCd 값이 searchInquiry 객체에 제대로 바인딩되었는지 확인용 로그
        System.out.println("Received inqryTrgtTypeCd at Controller: " + searchInquiry.getInqryTrgtTypeCd());

        return "admin/inquiry/adminInquiryList"; // 뷰 경로 확인
    }

    // 관리자 문의 상세 조회
    @GetMapping("/adminInquiryDetail")
    public String adminInquiryDetail(Model model,
                                      @RequestParam(name = "inqryId") String inqryId) {
        log.info("관리자 문의 상세 요청 - inqryId: {}", inqryId);

        Inquiry inquiryDetail = adminInquiryService.getInquiryDetailById(inqryId);
        if (inquiryDetail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.");
        }

        model.addAttribute("title", "관리자 문의 상세");
        model.addAttribute("inquiryDetail", inquiryDetail);

        return "admin/inquiry/adminInquiryDetailView";
    }

    // 답변 등록 또는 수정 처리 (AJAX 요청)
    @PostMapping("/answerProcess")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAnswer(
            @RequestParam("inqryId") String inqryId,
            @RequestParam("ansCn") String ansCn,
            @RequestParam(name = "ansId", required = false) String ansId,
            @RequestParam(name = "answrUserNo") String answrUserNo
    ) {
        Map<String, Object> response = new HashMap<>();
        log.info("컨트롤러: 답변 처리 요청 수신 - inqryId: {}, ansCn: {}, ansId: {}, answrUserNo: {}", inqryId, ansCn, ansId, answrUserNo);

        try {
            Answer processedAnswer;
            if (ansId == null || ansId.isEmpty()) {
                log.info("컨트롤러: 신규 답변 등록 로직 실행");
                processedAnswer = adminInquiryService.registerAnswer(inqryId, ansCn, answrUserNo);
                response.put("message", "답변이 성공적으로 등록되었습니다.");
            } else {
                log.info("컨트롤러: 기존 답변 수정 로직 실행 - 대상 ansId: {}", ansId);
                processedAnswer = adminInquiryService.updateAnswer(ansId, inqryId, ansCn, answrUserNo);
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
                response.put("status", "error");
                response.put("message", "답변 처리 중 오류가 발생했습니다. (서비스 로직 실패)");
                log.error("컨트롤러: 답변 처리 실패 (서비스 로직에서 null 반환)");
            }

        } catch (Exception e) {
            log.error("컨트롤러: 답변 처리 중 예외 발생", e);
            response.put("status", "error");
            response.put("message", "답변 처리 중 예외가 발생했습니다: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
package ks55team02.admin.inquiry.controller;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;

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
            @RequestParam(name = "filterConditions", required = false) String filterConditionsString,
            @ModelAttribute Inquiry searchInquiry
    ) {
        log.info("관리자 문의 목록 요청 - currentPage: {}, pageSize: {}", searchInquiry.getCurrentPage(), searchInquiry.getPageSize());
        log.info("검색 조건: searchKey={}, searchValue={}, startDate={}, endDate={}, filterConditions={}, sortKey={}, sortOrder={}, inqryTrgtTypeCd={}",
                 searchInquiry.getSearchKey(), searchInquiry.getSearchValue(), searchInquiry.getStartDate(), searchInquiry.getEndDate(),
                 searchInquiry.getFilterConditions(), searchInquiry.getSortKey(), searchInquiry.getSortOrder(), searchInquiry.getInqryTrgtTypeCd());

        if (filterConditionsString != null && !filterConditionsString.isEmpty()) {
            List<String> filterConditions = Arrays.asList(filterConditionsString.split(","));
            searchInquiry.setFilterConditions(filterConditions);
            log.info("변환된 필터 조건: {}", filterConditions);
        } else {
            searchInquiry.setFilterConditions(null);
        }

        int totalInquiryCnt = adminInquiryService.getAdminInquiryCnt(searchInquiry);
        log.info("총 문의 개수: {}", totalInquiryCnt);

        Pagination pagination = new Pagination(totalInquiryCnt, searchInquiry);
        log.info("페이지네이션 정보: {}", pagination);

        searchInquiry.setOffset(pagination.getLimitStart());
        
        List<Inquiry> inquiryList = adminInquiryService.getAdminInquiryList(searchInquiry, pagination.getLimitStart(), pagination.getRecordSize());
        log.info("조회된 문의 목록: {}", inquiryList);

        List<String> processStatuses = Arrays.asList("접수", "완료", "진행중");
        model.addAttribute("processStatuses", processStatuses);

        model.addAttribute("title", "관리자 문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchInquiry);

        model.addAttribute("filterConditionsString", filterConditionsString); 
        
        System.out.println("Received inqryTrgtTypeCd at Controller: " + searchInquiry.getInqryTrgtTypeCd());

        return "admin/inquiry/adminInquiryList";
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
        
        model.addAttribute("answerApiUrl", "/admin/inquiry/answerProcess");

        return "admin/inquiry/adminInquiryDetailView";
    }

    // 답변 등록 또는 수정 처리 (AJAX 요청)
    @PostMapping("/answerProcess")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAnswer(
            @RequestParam("inqryId") String inqryId,
            @RequestParam("ansCn") String ansCn,
            @RequestParam(name = "ansId", required = false) String ansId,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        // --- 세션에서 로그인된 관리자 정보 가져오는 로직 시작 ---
        String answrUserNo = null; // answrUserNo 변수에 실제 사용자 번호(userNo)를 할당할 예정
        HttpSession session = request.getSession(false); // 기존 세션이 없으면 새로 생성하지 않음 (false)

        if (session != null) {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser"); // 세션에서 LoginUser 객체 조회
            
            if (loginUser != null) {
                // 로그인된 관리자 닉네임은 로그 출력용으로만 사용합니다.
                log.info("로그인 성공: {}", loginUser.getUserNcnm());
                // 필요하다면 세션에 다시 설정할 수 있지만, 일반적으로 조회 후 사용이 더 일반적입니다.
                // session.setAttribute("loginUser", loginUser); 
                
                // **여기서 중요한 변경: answrUserNo에 userNcnm 대신 userNo를 할당합니다.**
                answrUserNo = loginUser.getUserNo(); // 외래 키 제약 조건에 맞는 실제 user_no 값 할당
                
                String adminUserNoLog = loginUser.getUserNo(); // 로그 출력용 변수
                String adminNickLog = loginUser.getUserNcnm(); // 로그 출력용 변수
                log.info("로그인 성공 - 관리자 번호: {}, 닉네임: {}", adminUserNoLog, adminNickLog);
            }
        }

        // 로그인된 사용자 번호(userNo)가 없으면 오류 처리 (로그인되지 않은 상태)
        if (answrUserNo == null || answrUserNo.isEmpty()) {
            log.error("로그인된 사용자 정보 (사용자 번호)를 찾을 수 없습니다. 로그인이 필요합니다.");
            response.put("status", "error");
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // --- 세션에서 로그인된 관리자 정보 가져오는 로직 끝 ---

        // 로그 메시지도 answrUserNo가 사용자 번호임을 명확히 합니다.
        log.info("컨트롤러: 답변 처리 요청 수신 - inqryId: {}, ansCn: {}, ansId: {}, answrUserNo (로그인된 관리자 번호): {}", inqryId, ansCn, ansId, answrUserNo);

        try {
            Answer processedAnswer;
            if (ansId == null || ansId.isEmpty()) {
                log.info("컨트롤러: 신규 답변 등록 로직 실행");
                // answrUserNo는 이제 세션에서 가져온 관리자 번호입니다.
                processedAnswer = adminInquiryService.registerAnswer(inqryId, ansCn, answrUserNo); 
                response.put("message", "답변이 성공적으로 등록되었습니다.");
            } else {
                log.info("컨트롤러: 기존 답변 수정 로직 실행 - 대상 ansId: {}", ansId);
                // answrUserNo는 이제 세션에서 가져온 관리자 번호입니다.
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
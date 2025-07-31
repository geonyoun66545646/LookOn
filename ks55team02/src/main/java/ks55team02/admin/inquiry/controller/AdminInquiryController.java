package ks55team02.admin.inquiry.controller;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.inquiry.service.AdminInquiryService;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 문의 관련 뷰(페이지)와 API(AJAX) 요청을 모두 처리하는 통합 컨트롤러
 */
@Controller
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Slf4j
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    /* =======================================================
     *  View 반환 메소드 (페이지 로딩)
     * ======================================================= */

    /**
     * 관리자 문의 목록 페이지를 조회합니다.
     */
    @GetMapping("/adminInquiryList")
    public String adminInquiryList(
            Model model,
            @RequestParam(name = "filterConditions", required = false) String filterConditionsString,
            @ModelAttribute("searchCriteria") Inquiry searchCriteria
    ) {
        log.info("관리자 문의 목록 페이지 요청");

        // [수정] 서비스로 넘기기 전에, filterConditions를 Inquiry 객체에 설정
        if (filterConditionsString != null && !filterConditionsString.isEmpty()) {
            searchCriteria.setFilterConditions(Arrays.asList(filterConditionsString.split(",")));
        } else {
            searchCriteria.setFilterConditions(null);
        }

        // 서비스 호출 (기존과 동일)
        int totalInquiryCnt = adminInquiryService.getAdminInquiryCnt(searchCriteria);
        Pagination pagination = new Pagination(totalInquiryCnt, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart());
        List<Inquiry> inquiryList = adminInquiryService.getAdminInquiryList(searchCriteria, pagination.getLimitStart(), pagination.getRecordSize());

        // [핵심 수정] 뷰에 전달할 상태 목록 Map을 실제 DB 값과 일치시킴
        Map<String, String> processStatuses = new LinkedHashMap<>();
        processStatuses.put("RECEPTION", "접수");
        processStatuses.put("COMPLETED", "완료");
        
        /* 기간 기본 값. */
        // 1. 시작 날짜가 비어있는지(null) 확인합니다.
        if (searchCriteria.getStartDate() == null) {
            searchCriteria.setStartDate(LocalDate.parse("2020-01-01"));
        }

        // 2. 종료 날짜가 비어있는지(null) 확인합니다.
        if (searchCriteria.getEndDate() == null) {
            searchCriteria.setEndDate(LocalDate.now());
        }

        // 모델에 데이터 추가
        model.addAttribute("title", "관리자 문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("filterConditionsString", filterConditionsString);
        model.addAttribute("processStatuses", processStatuses); // Map 전달
        model.addAttribute("searchCriteria", searchCriteria);
        
        return "admin/inquiry/adminInquiryList";
    }
    /**
     * 관리자 문의 상세 페이지를 조회합니다.
     */
    @GetMapping("/adminInquiryDetail")
    public String adminInquiryDetail(
            Model model,
            @RequestParam(name = "inqryId") String inqryId) {
        log.info("관리자 문의 상세 페이지 요청 - inqryId: {}", inqryId);
        Inquiry inquiryDetail = adminInquiryService.getInquiryDetailById(inqryId);
        if (inquiryDetail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.");
        }
        model.addAttribute("title", "관리자 문의 상세");
        model.addAttribute("inquiryDetail", inquiryDetail);
        model.addAttribute("answerApiUrl", "/admin/inquiry/answer");
        return "admin/inquiry/adminInquiryDetailView";
    }

    /* =======================================================
     *  API 메소드 (AJAX 데이터 처리)
     * ======================================================= */
    
    /**
     * 답변을 등록하거나 수정하는 API
     */
    @PostMapping("/answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAnswer(
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        // [확인] 로그인 여부만 확인 (권한 확인은 인터셉터 등 다른 곳에서 처리)
        if (loginUser == null) {
            response.put("status", "error");
            response.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        String adminUserNo = loginUser.getUserNo();
        String inqryId = payload.get("inqryId");
        String ansCn = payload.get("ansCn");
        String ansId = payload.get("ansId");

        log.info("API: 답변 처리 요청 - inqryId: {}, ansId: {}, adminUserNo: {}", inqryId, ansId, adminUserNo);
        try {
            boolean isNew = (ansId == null || ansId.isEmpty());
            Answer processedAnswer = isNew 
                ? adminInquiryService.registerAnswer(inqryId, ansCn, adminUserNo) 
                : adminInquiryService.updateAnswer(ansId, inqryId, ansCn, adminUserNo);

            response.put("status", "success");
            response.put("message", isNew ? "답변이 성공적으로 등록되었습니다." : "답변이 성공적으로 수정되었습니다.");
            response.put("ansId", processedAnswer.getAnsId());
            response.put("ansCn", processedAnswer.getAnsCn());
            response.put("answrUserNo", processedAnswer.getAnswrUserNo());
            response.put("ansTm", processedAnswer.getAnsTm() != null ? processedAnswer.getAnsTm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            response.put("lastMdfcnDt", processedAnswer.getLastMdfcnDt() != null ? processedAnswer.getLastMdfcnDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("API: 답변 처리 중 예외 발생", e);
            response.put("status", "error");
            response.put("message", "답변 처리 중 예외가 발생했습니다.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 선택된 문의들의 처리 상태를 일괄 변경하는 API
     */
    @PostMapping("/updateInquiryStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInquiryStatus(
            @RequestBody Map<String, Object> payload, 
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        // [확인] 로그인 여부만 확인 (권한 확인은 인터셉터 등 다른 곳에서 처리)
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        String adminUserNo = loginUser.getUserNo();
        
        try {
            List<String> inquiryIds = (List<String>) payload.get("inquiryIds");
            String newStatus = (String) payload.get("newStatus");

            adminInquiryService.updateInquiryStatus(inquiryIds, newStatus, adminUserNo);

            response.put("success", true);
            response.put("message", "선택한 문의의 상태가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("API: 문의 상태 변경 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 내부 오류로 인해 작업에 실패했습니다.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
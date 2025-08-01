package ks55team02.customer.inquiry.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.store.Store;
import ks55team02.customer.inquiry.domain.InquiryOption;
import ks55team02.customer.inquiry.domain.InquiryTargetOption;
import ks55team02.customer.inquiry.service.InquiryService;
import ks55team02.customer.login.domain.LoginUser; // LoginUser 클래스 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/customer/inquiry")
@Controller
@RequiredArgsConstructor
@Slf4j
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 현재 로그인된 사용자 ID를 세션에서 가져오는 유틸리티 메서드
     *
     * @param session 현재 HTTP 세션
     * @return 로그인된 사용자 ID 또는 로그인 정보가 없으면 null
     */
    private String getCurrentUserId(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        if (loginUser != null) {
            String userNo = loginUser.getUserNo();
            log.info("현재 로그인된 사용자 번호: {}", userNo);
            return userNo;
        } else {
            log.warn("세션에 로그인된 사용자 정보 (LoginUser)가 없습니다.");
            return null;
        }
    }

    // 자주묻는 질문 페이지
    @GetMapping("/FAQ")
    public String frequentlyAskedQuestionsView() {
        return "customer/inquiry/frequentlyAskedQuestionsView";
    }

    /**
     * 문의 상세 정보를 조회하는 메서드입니다.
     * 비밀글인 경우, 작성자 또는 관리자만 접근 가능하도록 권한을 확인합니다.
     *
     * @param inquiryId 조회할 문의의 고유 ID
     * @param model 뷰로 데이터를 전달하는 Model 객체
     * @param session 현재 HTTP 세션 (로그인 사용자 ID를 가져오기 위해 사용)
     * @param redirectAttributes 리다이렉트 시 메시지를 전달하기 위해 사용
     * @return 문의 상세 페이지 뷰 이름 또는 리다이렉트 경로
     */
    @GetMapping("/inquiryDetail")
    public String getInquiryDetail(@RequestParam("inquiryId") String inquiryId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        //1. 현재 로그인된 사용자 ID를 가져옵니다.
        String currentUserId = getCurrentUserId(session);

        //2. 로그인 여부 확인 (주석 해제)
        if(currentUserId == null){
            log.warn("문의 상세 조회 시도 - 로그인되지 않은 사용자 접근.");
            redirectAttributes.addFlashAttribute("errorMessage","로그인 후 문의를 열람 가능합니다.");
            redirectAttributes.addFlashAttribute("showAlert", true);
            return "redirect:/customer/login"; // 로그인 페이지 경로로 리다이렉트 (경로가 올바른지 확인 필요)
        }

        //3. InquiryService를 호출하여 문의 정보를 가져옵니다.
        // 이때 문의 ID와 함께 현재 로그인된 사용자 ID를 전달하여 서비스 계층에서 비밀글 권한을 확인하도록 합니다.
        Inquiry inquiry = inquiryService.getInquiryById(inquiryId, currentUserId);

        //4. 서비스 계층에서 반환된 Inquiry 객체가 null 인지 확인합니다.
        // - null인 경우: 해당 문의를 찾을 수 없거나, 비밀글인데 현재 사용자가 열람 권한이 없는 경우.
        if(inquiry == null) {
            log.warn("문의 상세 조회 실패: ID = {}, 사용자 = {}. 사유: 찾을 수 없거나 권한 없음.", inquiryId, currentUserId);
            redirectAttributes.addFlashAttribute("errorMessage", "해당 문의를 찾을 수 없거나 열람 권한이 없습니다.");
            redirectAttributes.addFlashAttribute("showAlert", true);
            return "redirect:/customer/inquiry/inquiryList";
        }
        //5. 문의 정보가 정상적으로 조회되었고 권한도 확인된 경우,
        model.addAttribute("title", "특정 문의 조회");
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("currentUserId", currentUserId); // JSP에서 현재 사용자 ID를 활용할 수 있도록 전달 (예: 자신의 글에만 수정/삭제 버튼 노출)
        return "customer/inquiry/inquiryDetailView";
    }

    /**
     * 문의 등록 폼 & 문의 옵션을 보여주는 메서드입니다.
     *
     * @param model 뷰로 데이터를 전달하는 Model 객체
     * @param session 현재 HTTP 세션 (로그인 사용자 ID를 가져오기 위해 사용)
     * @param redirectAttributes 리다이렉트 시 메시지를 전달하기 위해 사용
     * @return 문의 등록 폼 뷰 이름 또는 리다이렉트 경로
     */
    @GetMapping("/addInquiry")
    public String addInquiry(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        //1 현재 로그인된 사용자 ID를 가져옵니다
        String currentUserId = getCurrentUserId(session);

        // 2. 로그인 여부 확인 (주석 해제)
        if (currentUserId == null) {
            log.warn("로그인되지 않은 사용자가 문의 등록 폼에 접근 시도.");
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 문의를 작성할 수 있습니다.");
            redirectAttributes.addFlashAttribute("showAlert", true);
            return "redirect:/customer/login"; // 로그인 페이지로 리다이렉트 (경로가 올바른지 확인 필요)
        }
        //3. Model에 필요한 데이터를 담아 뷰로 전달합니다.
        model.addAttribute("title", "문의 등록");
        model.addAttribute("inquiry", new Inquiry());
        
        // 문의 대상(상점/관리자) 옵션 추가
        List<InquiryTargetOption> inquiryTargetOptions = inquiryService.getInquiryTargetOptions();
        model.addAttribute("inquiryTargetOptions", inquiryTargetOptions);
        
        // 관리자 문의 유형 옵션( 상품, 배송, SNS, 기타)
        List<InquiryOption> adminInquiryOptions = inquiryService.getAdminInquiryOptions();
        model.addAttribute("adminInquiryOptions", adminInquiryOptions);
        model.addAttribute("inquiryOption", InquiryOption.values());
        
        // 상점 목록 추가(상점 문의)
        List<Store> storeList = inquiryService.getStoreList();
        model.addAttribute("storeList", storeList);
        
        model.addAttribute("currentUserId", currentUserId);
        return "customer/inquiry/addInquiryView";
    }

    /**
     * 클라이언트로부터 문의 등록 요청을 받아 처리하는 POST 메서드입니다. (AJAX 요청 처리)
     *
     * @param inquiry HTML 폼에서 전송된 문의 데이터를 바인딩한 Inquiry 객체
     * @param attachedFiles 폼에서 첨부된 파일들 (MultipartFile 배열)
     * @param session 현재 HTTP 세션 (로그인 사용자 ID를 가져오기 위해 사용)
     * @return JSON 형식의 응답 (성공/실패 메시지)
     */
    @ResponseBody // JSON 응답을 위해 사용
    @PostMapping("/addInquiry")
    public ResponseEntity<Map<String, Object>> addInquiry(
            Inquiry inquiry,
            @RequestPart(name="attachedFiles", required=false) MultipartFile[] attachedFiles,
            HttpSession session) {
        log.info("AJAX 요청 수신된 문의 정보 (전체): {}", inquiry);
        log.info("AJAX 요청 수신된 문의 prvtYn 값: {}", inquiry.isPrvtYn());
        log.info("AJAX 요청 수신된 문의 inqryTypeCd 값: {}", inquiry.getInqryTypeCd()); // 세부 유형 (상품, 배송 등)
        log.info("AJAX 요청 수신된 문의 inqryTrgtTypeCd 값: {}", inquiry.getInqryTrgtTypeCd()); // 문의 대상 (상점, 관리자)
        log.info("AJAX 요청 수신된 문의 inqryStoreId 값: {}", inquiry.getInqryStoreId()); // 선택된 상점 ID
        log.info("수신된 첨부 파일 개수: {}", attachedFiles != null ? attachedFiles.length : 0);

        //1. 현재 로그인된 사용자 ID를 가져옵니다
        String currentUserId = getCurrentUserId(session);

        // 2. 로그인 여부 확인 (주석 해제)
        if (currentUserId == null) {
            log.warn("로그인되지 않은 사용자가 문의 등록을 시도.");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "로그인 후 문의를 작성할 수 있습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // 401 Unauthorized 응답
        }

        Map<String, Object> response = new HashMap<>();
        try {
            //3. InquiryService를 호출하여 문의를 등록합니다.
            // 이때, 문의 데이터와 첨부파일, 그리고 현재 로그인된 사용자 ID를 함께 전달합니다
            // 서비스 계층에서 이 ID를 문의의 작성자 ID로 설정합니다.
            inquiryService.addInquiry(inquiry, attachedFiles, currentUserId);

            //4. 등록 성공 시 성공 응답을 반환합니다
            response.put("status", "success");
            response.put("message", "등록 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            //5. 예외 발생 시 에러 응답을 반환합니다.
            log.error("문의 등록 실패 (AJAX): {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "문의 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 문의 목록을 페이징하여 조회하는 메서드입니다.
     *
     * @param model 뷰로 데이터를 전달하는 Model 객체
     * @param currentPage 현재 페이지 번호 (기본값: 1)
     * @param pageSize 페이지당 항목 수 (기본값: 15)
     * @param session 현재 HTTP 세션 (JSP에서 로그인 사용자 ID를 활용하기 위해 사용)
     * @return 문의 목록 페이지 뷰 이름
     */
    // 문의 목록 조회 (페이징 적용 버전)
    @GetMapping("/inquiryList")
    public String getInquiryList(
            Model model,
            @RequestParam(name = "page", defaultValue = "1") int currentPage,
            @RequestParam(name = "size", defaultValue = "15") int pageSize,
            HttpSession session // 현재 사용자 ID를 활용 할 수 있도록 세션 추가
    ) {
        //1. InquiryService를 호출하여 페이징된 문의 목록과 전체 개수 등 데이터를 가져옵니다.
        Map<String, Object> pagingData = inquiryService.getInquiryList(currentPage, pageSize);

        //2. 조회된 데이터 파싱
        List<Inquiry> inquiryList = (List<Inquiry>) pagingData.get("inquiryList");
        int totalRows = (int) pagingData.get("totalRows");

        //3. 페이징 관련 계산
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        int pageBlockSize = 5;
        int startPage = ((currentPage - 1) / pageBlockSize) * pageBlockSize + 1;
        int endPage = Math.min(startPage + pageBlockSize - 1, totalPages);
        

        //5. Model에 데이터를 담아 뷰로 전달합니다.
        model.addAttribute("title", "문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalRows", totalRows);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentUserId", getCurrentUserId(session));

        return "customer/inquiry/inquiryListView";
    }
    
 // ======================= [AJAX] 문의 수정 처리 (POST) =======================
    @ResponseBody // JSON 응답을 위해 필수
    @PostMapping("/updateInquiryAjax")
    public ResponseEntity<Map<String, Object>> updateInquiryAjax(@RequestBody Inquiry inquiry, // JSON 데이터를 받기 위해 @RequestBody 사용
                                                                   HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String currentUserId = getCurrentUserId(session);
        
        if (currentUserId == null) {
            response.put("status", "error");
            response.put("message", "세션이 만료되었습니다. 다시 로그인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // 서비스의 수정 메서드 호출 (inquiry 객체와 사용자 ID 전달)
            // 서비스에서 권한 검사 및 답변 여부 검사를 수행합니다.
            inquiryService.updateInquiry(inquiry, currentUserId);
            
            response.put("status", "success");
            response.put("message", "문의가 성공적으로 수정되었습니다.");
            // 수정된 내용을 utext로 변환하여 클라이언트에 보내주면 바로 화면에 적용 가능
            response.put("updatedContentHtml", inquiry.getInqryCn().replace("\n", "<br>"));
            return ResponseEntity.ok(response);

        } catch (SecurityException | IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            log.error("문의 수정(AJAX) 중 오류 발생", e);
            response.put("status", "error");
            response.put("message", "문의 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ======================= [AJAX] 문의 삭제 처리 (POST) =======================
    @ResponseBody // JSON 응답을 위해 필수
    @PostMapping("/deleteInquiryAjax")
    public ResponseEntity<Map<String, Object>> deleteInquiryAjax(@RequestBody Map<String, String> payload, // inquiryId를 받기 위함
                                                                   HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String inquiryId = payload.get("inquiryId");
        String currentUserId = getCurrentUserId(session);

        if (currentUserId == null) {
            response.put("status", "error");
            response.put("message", "세션이 만료되었습니다. 다시 로그인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // 서비스의 삭제 메서드 호출
            inquiryService.deleteInquiry(inquiryId, currentUserId);
            
            response.put("status", "success");
            response.put("message", "문의가 삭제되었습니다.");
            // 삭제 후 이동할 URL 전달
            response.put("redirectUrl", "/customer/inquiry/inquiryList");
            return ResponseEntity.ok(response);

        } catch (SecurityException | IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            log.error("문의 삭제(AJAX) 중 오류 발생", e);
            response.put("status", "error");
            response.put("message", "문의 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
}
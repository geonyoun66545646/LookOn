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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
    public String getInquiryDetail(@RequestParam("inquiryId") String inquiryId, Model model) {

        Inquiry inquiry = inquiryService.getInquiryById(inquiryId);

        model.addAttribute("title", "특정 문의 조회");
        model.addAttribute("inquiry", inquiry);

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
    @ResponseBody
    @PostMapping("/addInquiry")
    public ResponseEntity<Map<String, Object>> addInquiry(Inquiry inquiry,
    		@RequestParam(name="attachedFiles", required=false)List<MultipartFile>attachedFiles) {
        log.info("AJAX 요청 수신된 문의 정보 (전체): {}", inquiry);
        log.info("AJAX 요청 수신된 문의 prvtYn 값: {}", inquiry.isPrvtYn());
        log.info("AJAX 요청 수신된 문의 inqryTypeCd 값: {}", inquiry.getInqryTypeCd());
        log.info("수신된 첨부 파일 개수: {}", attachedFiles != null ? attachedFiles.size() : 0); // 파일 수신 확인 로그


        Map<String, Object> response = new HashMap<>();
        try {
            inquiryService.addInquiry(inquiry, attachedFiles);

            response.put("status", "success");
            response.put("message", "등록 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("문의 등록 실패 (AJAX): {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "문의 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 문의 목록 조회 (페이징 적용 버전만 남김)
    @GetMapping("/inquiryList")
    public String getInquiryList(
            Model model,
            @RequestParam(name = "page", defaultValue = "1") int currentPage,
            @RequestParam(name = "size", defaultValue = "15") int pageSize
    ) {
        Map<String, Object> pagingData = inquiryService.getInquiryList(currentPage, pageSize); // 이 메서드가 페이징 처리된 데이터를 반환

        List<Inquiry> inquiryList = (List<Inquiry>) pagingData.get("inquiryList");
        int totalRows = (int) pagingData.get("totalRows");

        int totalPages = (int) Math.ceil((double) totalRows / pageSize);

        int pageBlockSize = 5; // 한 블록에 보여줄 페이지 수
        int startPage = ((currentPage - 1) / pageBlockSize) * pageBlockSize + 1;
        int endPage = Math.min(startPage + pageBlockSize - 1, totalPages);

        model.addAttribute("title", "문의 목록");
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalRows", totalRows);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "customer/inquiry/inquiryListView";
    }
}
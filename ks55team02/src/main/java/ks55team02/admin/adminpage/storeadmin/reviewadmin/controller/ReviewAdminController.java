package ks55team02.admin.adminpage.storeadmin.reviewadmin.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession; 

import ks55team02.common.domain.store.ProductReview;
import ks55team02.admin.adminpage.storeadmin.reviewadmin.service.ReviewAdminService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.customer.login.domain.LoginUser; 
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
@Log4j2
public class ReviewAdminController {

    private final ReviewAdminService reviewAdminService;

    // reviewAdminManagementView 메소드는 기존과 동일
    @GetMapping("/reviewAdmin")
    public String reviewAdminManagementView(
            Model model,
            ProductReview review
    ) {
        log.info("컨트롤러 진입: filterConditions = {}", review.getFilterConditions());

        // [핵심 수정] 사용자가 필터를 선택하지 않았거나, 최초 접속 시 기본으로 '활성'과 '숨김' 상태만 조회하도록 설정
        if (review.getFilterConditions() == null || review.getFilterConditions().isEmpty()) {
            review.setFilterConditions(List.of("ACTIVE", "HIDDEN"));
            log.info("기본 필터 적용: ACTIVE, HIDDEN");
        }

        int totalRecordCount = reviewAdminService.getReviewCount(review);
        Pagination pagination = new Pagination(totalRecordCount, review);
        review.setOffset(pagination.getLimitStart());
        List<ProductReview> reviewList = reviewAdminService.getReviewList(review);

        log.info("최종 조회 조건: {}", review);
        log.info("조회된 리뷰 개수: {}", reviewList.size());
        
        model.addAttribute("title", "상품 리뷰 관리");
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", review); // 모델에 담는 searchCriteria에는 기본 필터가 적용된 상태

        return "admin/adminpage/storeadmin/reviewAdminView";
    }


    // [수정] updateReviewStatus 메소드
    @PostMapping("/updateReviewStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReviewStatus(
            @RequestBody Map<String, Object> payload,
            HttpSession session
    ) {
        try {
            @SuppressWarnings("unchecked")
            List<String> reviewIds = (List<String>) payload.get("reviewIds");
            // [수정] newStatus를 String으로 받음 (AJAX에서 'ACTIVE', 'HIDDEN', 'DELETED' 전송)
            String newStatus = (String) payload.get("newStatus");

            if (reviewIds == null || reviewIds.isEmpty() || newStatus == null || newStatus.isBlank()) {
                return new ResponseEntity<>(Map.of("success", false, "message", "필수 파라미터가 누락되었습니다."), HttpStatus.BAD_REQUEST);
            }

            // 세션에서 로그인된 사용자 ID 가져오기
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            if (loginUser == null) {
                // 관리자 페이지이므로 로그인이 안된 경우는 비정상 접근으로 간주 가능
                return new ResponseEntity<>(Map.of("success", false, "message", "로그인 정보가 없습니다. 다시 로그인해주세요."), HttpStatus.UNAUTHORIZED);
            }
            String loggedInUserLgnId = loginUser.getUserLgnId();

            // [수정] 서비스 호출 (일괄 처리 메소드 호출)
            reviewAdminService.updateReviewStatusBatch(reviewIds, newStatus, loggedInUserLgnId);

            return new ResponseEntity<>(Map.of("success", true, "message", "리뷰 상태가 성공적으로 변경되었습니다."), HttpStatus.OK);
        } catch (Exception e) {
            log.error("리뷰 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>(Map.of("success", false, "message", "리뷰 상태 변경 중 내부 서버 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
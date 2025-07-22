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

    @GetMapping("/reviewAdmin")
    public String reviewAdminManagementView(
            Model model,
            ProductReview review
    ) {
        log.info("컨트롤러: reviewAdminManagementView 호출 - 현재 페이지: {}, 페이지 크기: {}, 검색 키: {}, 검색 값: {}",
                 review.getCurrentPage(), review.getPageSize(), review.getSearchKey(), review.getSearchValue());
        log.info("디버그 확인: review.filterConditions = {}", review.getFilterConditions());

        int totalRecordCount = reviewAdminService.getReviewCount(review);

        Pagination pagination = new Pagination(totalRecordCount, review);
        log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
                 pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());

        review.setOffset(pagination.getLimitStart());

        List<ProductReview> reviewList = reviewAdminService.getReviewList(review, pagination.getLimitStart(), review.getPageSize());
        log.info("컨트롤러: reviewList 조회 결과 개수: {}", reviewList.size());

        model.addAttribute("title", "상품 리뷰 관리");
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", review);

        return "admin/adminpage/storeadmin/reviewAdminView";
    }

    @PostMapping("/updateReviewStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReviewStatus(
            @RequestBody Map<String, Object> payload,
            HttpSession session // HttpSession 파라미터 추가
    ) {
        try {
            @SuppressWarnings("unchecked")
            List<String> reviewIds = (List<String>) payload.get("reviewIds");
            Integer newStatus = (Integer) payload.get("newStatus");

            if (reviewIds == null || reviewIds.isEmpty() || newStatus == null) {
                return new ResponseEntity<>(Map.of("success", false, "message", "필수 파라미터가 누락되었습니다."), HttpStatus.BAD_REQUEST);
            }

            String loggedInUserLgnId = null;
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser"); // 세션에서 LoginUser 객체 가져오기

            if (loginUser != null) {
                loggedInUserLgnId = loginUser.getUserLgnId(); // LoginUser 객체에서 로그인 ID 가져오기
            } else {
                log.warn("로그인된 사용자 정보를 세션에서 찾을 수 없습니다. del_prcr_no는 NULL로 설정될 수 있습니다.");
            }

            reviewAdminService.updateReviewStatus(reviewIds, newStatus, loggedInUserLgnId);

            return new ResponseEntity<>(Map.of("success", true, "message", "리뷰 상태가 성공적으로 변경되었습니다."), HttpStatus.OK);
        } catch (Exception e) {
            log.error("리뷰 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>(Map.of("success", false, "message", "리뷰 상태 변경 중 내부 서버 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

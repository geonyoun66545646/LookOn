package ks55team02.seller.review.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.common.domain.Pagination;
import ks55team02.common.domain.store.ProductReview;
import ks55team02.seller.review.service.ReviewSellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/seller/review")
@RequiredArgsConstructor
@Log4j2
public class ReviewSellerController {
	
    private final ReviewSellerService reviewSellerService;

    @GetMapping("/reviewList")
    public String reviewSellertView(
            Model model,
            ProductReview review
    ) {
        log.info("컨트롤러 진입: filterConditions = {}", review.getFilterConditions());
        
        //정렬 조건이 없을 경우 기본값으로 최신순(작성일 기준 내림차순) 정렬 적용
        if (review.getSortKey() == null || review.getSortKey().isEmpty()) {
            review.setSortKey("wrtYmd"); // 작성일 기준으로 정렬
        }
        if (review.getSortOrder() == null || review.getSortOrder().isEmpty()) {
            review.setSortOrder("DESC"); // 내림차순 (최신순)
        }

        int totalRecordCount = reviewSellerService.getSellerReviewCount(review);
        Pagination pagination = new Pagination(totalRecordCount, review);
        review.setOffset(pagination.getLimitStart());
        List<ProductReview> reviewList = reviewSellerService.getSellerReviewList(review);

        log.info("최종 조회 조건: {}", review);
        log.info("조회된 리뷰 개수: {}", reviewList.size());
        
        /* 기본 기간 설정 */
        if (review.getStartDate() == null) {
        	review.setStartDate(LocalDate.parse("2020-01-01"));
        }

        // 2. 종료 날짜가 비어있는지(null) 확인합니다.
        if (review.getEndDate() == null) {
        	review.setEndDate(LocalDate.now());
        }
        
        model.addAttribute("title", "상품 리뷰 관리");
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", review); // 모델에 담는 searchCriteria에는 기본 필터가 적용된 상태

        return "seller/review/reviewSellerView.html";
    }

}

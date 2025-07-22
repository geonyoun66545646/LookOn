package ks55team02.customer.store.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.common.domain.store.ProductReview;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.customer.store.service.ReviewService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 리뷰 관련 요청을 처리하는 컨트롤러
 * - @Controller를 사용하여 뷰(HTML) 반환과 데이터(JSON) 반환을 모두 처리합니다.
 * - API 경로는 /api/reviews 로 통일하여 관리합니다.
 */
@Log4j2
@Controller
@RequestMapping("/api/reviews") // API 경로를 명확하게 분리
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 '좋아요(도움이 돼요)' 상태를 토글하는 API
     * @param reviewId 좋아요를 누른 리뷰의 ID
     * @param session 현재 로그인한 사용자 정보를 얻기 위한 세션
     * @return 갱신된 좋아요 상태와 총 좋아요 수를 담은 JSON 응답
     */
    @PostMapping("/{reviewId}/like")
    @ResponseBody // 데이터(JSON)를 직접 반환함을 명시
    public ResponseEntity<Map<String, Object>> toggleReviewLike(
            @PathVariable("reviewId") String reviewId,
            HttpSession session) {

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "리뷰에 '좋아요'를 누르려면 로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        String userNo = loginUser.getUserNo();

        try {
            boolean isCurrentlyLiked = reviewService.toggleReviewLike(reviewId, userNo);
            int likeCount = reviewService.countReviewLikes(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isCurrentlyLiked);
            // JavaScript에서 사용하는 키(key) 이름과 일치시킴
            response.put("likeCount", likeCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("리뷰 좋아요 처리 중 오류 발생: reviewId={}, userNo={}", reviewId, userNo, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "서버 오류로 인해 요청을 처리할 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 상품 리뷰 목록을 페이지네이션과 함께 조회하는 API
     * @param gdsNo 상품 번호
     * @param page 요청 페이지 번호
     * @param session 현재 로그인한 사용자 정보를 확인하기 위한 세션
     * @return CustomerPagination 객체가 담긴 JSON 응답
     */
    @GetMapping("/products/{gdsNo}/reviews")
    @ResponseBody // 데이터(JSON)를 직접 반환함을 명시
    public ResponseEntity<CustomerPagination<ProductReview>> getProductReviews(
            @PathVariable("gdsNo") String gdsNo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String currentUserNo = (loginUser != null) ? loginUser.getUserNo() : null;
        
        int pageSize = 5;
        int blockSize = 10;

        long totalReviewCount = reviewService.getReviewCountByGdsNo(gdsNo);
        List<ProductReview> reviewList = reviewService.getReviewListPageByGdsNo(gdsNo, page, pageSize, currentUserNo);

        CustomerPagination<ProductReview> pagination = new CustomerPagination<>(
            reviewList, totalReviewCount, page, pageSize, blockSize
        );

        return ResponseEntity.ok(pagination);
    }
    
    /**
     * 리뷰를 등록하는 메소드 (페이지 이동 처리)
     * - 이 메소드는 @ResponseBody가 없으므로 redirect가 정상 동작합니다.
     */
    @PostMapping("/add")
    public String addReview(ReviewAddDto reviewAddDto, HttpSession session, RedirectAttributes redirectAttributes) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰를 작성하려면 로그인이 필요합니다.");
            return "redirect:/customer/login";
        }
        String currentUserNo = loginUser.getUserNo();

        try {
            reviewService.addReview(reviewAddDto, currentUserNo);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("리뷰 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
        
        return "redirect:/products/detail/" + reviewAddDto.getGdsNo();
    }
}
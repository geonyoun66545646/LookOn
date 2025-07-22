package ks55team02.customer.store.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.common.domain.store.ProductReview; // [중요] 매퍼의 resultMap과 일치하는 도메인 클래스를 사용합니다.
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.customer.store.service.ReviewService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/customer/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * [신규] 상품 리뷰 목록을 페이지네이션과 함께 조회하는 API
     * JavaScript의 fetch 요청을 이 메소드가 처리합니다.
     * @param gdsNo 상품 번호 (URL 경로에서 가져옴)
     * @param page 요청 페이지 번호 (쿼리 파라미터에서 가져옴)
     * @return CustomerPagination 객체가 담긴 JSON 응답
     */
    @GetMapping("/api/products/{gdsNo}/reviews")
    @ResponseBody // View가 아닌 데이터(JSON)를 반환하기 위한 어노테이션
    public ResponseEntity<CustomerPagination<ProductReview>> getProductReviews(
            @PathVariable("gdsNo") String gdsNo,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        
        int pageSize = 5;  // 한 페이지에 보여줄 리뷰 수
        int blockSize = 10; // 페이지네이션 블록에 보여줄 페이지 번호 수

        // 1. 서비스 레이어를 통해 전체 리뷰 개수 조회
        long totalReviewCount = reviewService.getReviewCountByGdsNo(gdsNo);

        // 2. 서비스 레이어를 통해 현재 페이지에 해당하는 리뷰 목록 조회
        List<ProductReview> reviewList = reviewService.getReviewListPageByGdsNo(gdsNo, page, pageSize);

        // 3. CustomerPagination 객체 생성
        CustomerPagination<ProductReview> pagination = new CustomerPagination<>(
            reviewList, totalReviewCount, page, pageSize, blockSize
        );

        // 4. 생성된 pagination 객체를 성공 상태(200 OK)와 함께 반환
        return ResponseEntity.ok(pagination);
    }
    

    /**
     * 리뷰를 등록하는 메소드 (기존 코드 유지)
     */
    @PostMapping("/add")
    public String addReview(ReviewAddDto reviewAddDto, HttpSession session, RedirectAttributes redirectAttributes) {
        // ... 기존 로직 그대로 ...
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
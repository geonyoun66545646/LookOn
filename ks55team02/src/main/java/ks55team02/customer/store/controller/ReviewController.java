// ks55team02.customer.store.controller.ReviewController.java
// 패키지 경로를 review 도메인에 맞게 변경하는 것이 더 좋습니다. 예: ks55team02.customer.review.controller
package ks55team02.customer.store.controller; // 패키지 경로 변경 권장

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.customer.store.service.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/customer/review") // 리뷰 관련 요청은 이 경로로 매핑됩니다.
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService; // ReviewService 주입
    

    /**
     * 리뷰 작성 폼 제출 처리
     * ProductReview DTO를 통해 폼 데이터를 받습니다.
     *
     * @param productReview 사용자가 작성한 리뷰 데이터 (reviewId는 자동 생성되므로 null로 들어올 수 있음)
     * @param redirectAttributes 리다이렉트 시 메시지 전달용
     * @return 상품 상세 페이지로 리다이렉트
     */
    @PostMapping("/add")
    public String addReview(ProductReview productReview, RedirectAttributes redirectAttributes) {
        // TODO:
        // 1. 로그인된 사용자 정보(prchsrUserNo)를 가져와 productReview에 설정
        // 2. reviewId 생성 (UUID나 시퀀스 등) 및 productReview에 설정
        // 3. wrtYmd, reviewStts 등 필요한 필드 설정 (lastMdfcnDt, delDt 등은 DB에서 DEFAULT/ON UPDATE로 설정되어 있다면 생략 가능)
        // 4. reviewService.addReview(productReview); 호출 (addReview 메서드는 ReviewService에 추가해야 함)

        System.out.println("리뷰 등록 요청: " + productReview); // 디버깅용

        // 예시: 임시 데이터 설정 (실제 구현 시 로그인 유저 정보 및 ID 생성 로직 필요)
        if (productReview.getPrchsrUserNo() == null || productReview.getPrchsrUserNo().isEmpty()) {
            productReview.setPrchsrUserNo("user001"); // 임시 사용자 ID
        }
        if (productReview.getReviewId() == null || productReview.getReviewId().isEmpty()) {
            productReview.setReviewId("review_" + System.currentTimeMillis()); // 임시 리뷰 ID
        }
        productReview.setReviewStts(true); // 초기 상태 설정 (필요에 따라)
        // wrtYmd는 @CreationTimestamp 등으로 DB에서 자동 생성되거나 서비스에서 설정

        // TODO: ReviewService에 addReview 메서드를 추가하고 호출
        // reviewService.addReview(productReview);

        redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 작성되었습니다.");
        // 리뷰 작성 후 해당 상품의 상세 페이지로 리다이렉트
        return "redirect:/products/detail/" + productReview.getGdsNo();
    }

    // TODO: 좋아요/취소, 리뷰 수정, 리뷰 삭제 등 다른 리뷰 관련 액션 메서드를 여기에 추가합니다.
    // @PostMapping("/like")
    // public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, String> payload) { ... }
}
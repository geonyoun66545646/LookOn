// ks55team02.customer.store.controller.ReviewController.java
// 패키지 경로를 review 도메인에 맞게 변경하는 것이 더 좋습니다. 예: ks55team02.customer.review.controller
package ks55team02.customer.store.controller; // 패키지 경로 변경 권장

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.customer.store.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/customer/review") // 리뷰 관련 요청은 이 경로로 매핑됩니다.
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService; // ReviewService 주입
    

    @PostMapping("/add")
    public String addReview(ReviewAddDto reviewAddDto, HttpSession session, RedirectAttributes redirectAttributes) {

        // 1. 세션에서 로그인 정보 확인
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰를 작성하려면 로그인이 필요합니다.");
            return "redirect:/customer/login"; // 로그인 페이지 경로
        }
        String currentUserNo = loginUser.getUserNo(); // 세션에서 사용자 번호 가져오기

        try {
            // 2. 서비스 레이어에 리뷰 추가 로직 위임
        	reviewService.addReview(reviewAddDto, currentUserNo);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 3. 서비스에서 발생한 검증 예외 처리
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // 4. 그 외 예상치 못한 에러 처리
            log.error("리뷰 등록 중 오류 발생", e); // 에러 로그를 상세하게 남깁니다.
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }

        // 5. 처리가 끝난 후, 원래 상품 상세 페이지로 리다이렉트
        // 상품 상세 페이지 URL이 '/customer/products/detail/{gdsNo}' 라고 가정
        return "redirect:/products/detail/" + reviewAddDto.getGdsNo();
    }
}
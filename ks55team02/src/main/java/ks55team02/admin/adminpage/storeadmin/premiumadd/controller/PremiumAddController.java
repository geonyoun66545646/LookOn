package ks55team02.admin.adminpage.storeadmin.premiumadd.controller;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;
import ks55team02.admin.adminpage.storeadmin.premiumadd.service.PremiumAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/premiumadd")
public class PremiumAddController {

	 private final PremiumAddService premiumAddService; // 서비스 타입 변경

	    @Autowired
	    public PremiumAddController(PremiumAddService premiumAddService) { // 생성자 주입 서비스 타입 변경
	        this.premiumAddService = premiumAddService;
	    }

	    /**
	     * 구독 플랜 등록 폼 페이지를 보여줍니다.
	     * URL: /admin/premiumadd/new
	     */
	    @GetMapping("/new")
	    public String showAddSubscriptionForm() {
	        return "admin/subscription/premiumAdd"; // Thymeleaf 템플릿 경로
	    }

	    /**
	     * 새로운 구독 플랜을 등록합니다. (폼 제출 처리)
	     * URL: /admin/premiumadd/new
	     * HTTP Method: POST
	     *
	     * @param premiumAddDTO 클라이언트에서 전송된 구독 플랜 정보 (PremiumAddDTO로 변경)
	     * @param rttr 리다이렉트 시 메시지를 전달하기 위한 객체
	     * @return 리다이렉트 경로
	     */
	    @PostMapping("/new")
	    public String registerSubscriptionPlan(@ModelAttribute PremiumAddDTO premiumAddDTO, RedirectAttributes rttr) {
	        boolean success = premiumAddService.registerSubscriptionPlan(premiumAddDTO);
	        if (success) {
	            rttr.addFlashAttribute("message", "구독 플랜 '" + premiumAddDTO.getSbscrPlanNm() + "'이(가) 성공적으로 등록되었습니다.");
	            rttr.addFlashAttribute("type", "success");
	        } else {
	            rttr.addFlashAttribute("message", "구독 플랜 등록에 실패했습니다. 입력값을 확인해주세요.");
	            rttr.addFlashAttribute("type", "error");
	        }
	        return "redirect:/admin/premiumadd/list"; // 등록 후 목록 페이지로 리다이렉트
	    }

	    /**
	     * 등록된 모든 구독 플랜 목록을 보여줍니다. (관리자용)
	     * URL: /admin/premiumadd/list
	     */
	    @GetMapping("/list")
	    public String listAllSubscriptionPlans(Model model) {
	        List<PremiumAddDTO> plans = premiumAddService.getAllPremiumAdds(); // 서비스 메소드명 변경 및 DTO 타입 변경
	        model.addAttribute("plans", plans); // 모델에 "plans"라는 이름으로 전달
	        return "admin/premium/premiumList"; // 모든 구독 플랜을 보여줄 Thymeleaf 템플릿 (예: premiumList.html)
	    }

	    // --- API 엔드포인트 예시 (Ajax 통신용) ---
	    // 만약 JavaScript에서 REST API처럼 데이터를 주고받고 싶다면 아래와 같이 @RestController를 사용하거나
	    // @ResponseBody를 메소드에 추가하여 JSON 응답을 보낼 수 있습니다.

	    /**
	     * AJAX 요청으로 새로운 구독 플랜을 등록합니다. (선택 사항)
	     * URL: /admin/premiumadd/api/add
	     * HTTP Method: POST
	     *
	     * @param premiumAddDTO 클라이언트에서 전송된 구독 플랜 정보 (JSON) (PremiumAddDTO로 변경)
	     * @return 성공 또는 실패 응답
	     */
	    @PostMapping("/api/add")
	    @ResponseBody // JSON 응답을 위해
	    public ResponseEntity<String> registerSubscriptionPlanApi(@RequestBody PremiumAddDTO premiumAddDTO) {
	        boolean success = premiumAddService.registerSubscriptionPlan(premiumAddDTO);
	        if (success) {
	            return ResponseEntity.ok("{\"message\": \"구독 플랜 등록 성공\"}");
	        } else {
	            return ResponseEntity.badRequest().body("{\"message\": \"구독 플랜 등록 실패\"}");
	        }
	    }
}
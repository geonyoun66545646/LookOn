package ks55team02.admin.adminpage.storeadmin.premiumadd.controller;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;
import ks55team02.admin.adminpage.storeadmin.premiumadd.service.PremiumAddService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // HttpStatus import를 위해 추가
import org.springframework.http.HttpStatus; // HttpStatus import를 위해 추가


import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
public class PremiumAddController {

    private final PremiumAddService premiumAddService;
    private static final Logger log = LoggerFactory.getLogger(PremiumAddController.class);

    // 구독 플랜 등록 폼
    @GetMapping("/premiumAdd")
    public String showAddSubscriptionForm() {
        return "admin/adminpage/storeadmin/premiumAdd";
    }

    // 구독 플랜 목록 조회
    @GetMapping("/list")
    public String listAllSubscriptionPlans(Model model) {
        List<PremiumAddDTO> plans = premiumAddService.getAllPremiumAdds();
        model.addAttribute("plans", plans);
        return "admin/adminpage/storeadmin/adminPremiumListView";
    }

    // [수정 1] GET 방식 완료 페이지 → 별도 경로로 분리
    @GetMapping("/premiumAddComplete")
    public String showCompletePage(@RequestParam(value = "name", required = false) String planName,
                                Model model) {
        model.addAttribute("sbscrPlanNm", planName != null ? planName : "새 플랜");
        return "admin/adminpage/storeadmin/premiumAddComplete";
    }

    // [수정 2] POST 방식 등록 처리 (실제 데이터 처리)
    @PostMapping("/premiumAdd/new")
    public String processSubscriptionPlan(@ModelAttribute PremiumAddDTO premiumAddDTO,
                                       Model model) {
        log.info("구독 플랜 등록 요청: {}", premiumAddDTO);

        try {
            boolean success = premiumAddService.registerSubscriptionPlan(premiumAddDTO);
            if (success) {
                // [수정 3] 리다이렉트로 변경 + 파라미터 전달
                return "redirect:/adminpage/storeadmin/premiumAddComplete?name="
                    + URLEncoder.encode(premiumAddDTO.getSbscrPlanNm(), StandardCharsets.UTF_8);
            } else {
                model.addAttribute("errorMessage", "등록에 실패했습니다.");
                return "admin/adminpage/storeadmin/premiumAdd";
            }
        } catch (Exception e) {
            log.error("구독 플랜 등록 오류", e);
            model.addAttribute("errorMessage", "서버 오류: " + e.getMessage());
            return "admin/adminpage/storeadmin/premiumAdd";
        }
    }

    // API용 구독 플랜 등록
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<String> registerSubscriptionPlanApi(@RequestBody PremiumAddDTO premiumAddDTO) {
        boolean success = premiumAddService.registerSubscriptionPlan(premiumAddDTO);
        if (success) {
            return ResponseEntity.ok("{\"message\": \"구독 플랜 등록 성공\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"구독 플랜 등록 실패\"}");
        }
    }

    // 구독 플랜 수정 폼 조회
    @GetMapping("/premiumModify/{id}")
    public String showModifyForm(@PathVariable("id") String planId, Model model) {
        PremiumAddDTO plan = premiumAddService.getPlanById(planId);

        if (plan == null) {
            // 해당 ID의 플랜을 찾을 수 없는 경우
            log.warn("프리미엄 플랜 ID {} 에 해당하는 플랜을 찾을 수 없습니다.", planId);
            // 404 Not Found 응답 또는 에러 페이지로 리다이렉트
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "프리미엄 플랜을 찾을 수 없습니다.");
            // 혹은 사용자에게 메시지를 보여줄 페이지로 리다이렉트
            // return "redirect:/errorPage?message=" + URLEncoder.encode("플랜을 찾을 수 없습니다.", StandardCharsets.UTF_8);
        }

        model.addAttribute("plan", plan);
        return "admin/adminpage/storeadmin/premiumModify";
    }

    // 구독 플랜 수정 처리
    @PostMapping("/premiumModify/{id}")
    public String processModifyPlan(
            @PathVariable("id") String planId,
            @ModelAttribute PremiumAddDTO modifiedPlan,
            Model model) {

        try {
            boolean success = premiumAddService.modifySubscriptionPlan(planId, modifiedPlan);
            if (success) {
                return "redirect:/adminpage/storeadmin/list";
            } else {
                model.addAttribute("errorMessage", "수정에 실패했습니다.");
                return "admin/adminpage/storeadmin/premiumModify";
            }
        } catch (Exception e) {
            log.error("구독 플랜 수정 오류", e);
            model.addAttribute("errorMessage", "서버 오류: " + e.getMessage());
            return "admin/adminpage/storeadmin/premiumModify";
        }
    }
    
 // 구독 플랜 삭제 처리 (AJAX 요청용)
    @PostMapping("/premiumDelete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSubscriptionPlan(@PathVariable("id") String planId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = premiumAddService.deleteSubscriptionPlan(planId);
            if (success) {
                response.put("success", true);
                response.put("message", "구독 플랜이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "구독 플랜 삭제에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("구독 플랜 삭제 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류로 인해 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
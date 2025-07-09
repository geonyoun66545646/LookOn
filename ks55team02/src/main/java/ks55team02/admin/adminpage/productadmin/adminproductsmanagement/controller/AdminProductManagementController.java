// 원래의 올바른 패키지 경로를 그대로 유지합니다.
package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductSearchCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductRejectRequest;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service.AdminProductManagementService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
// ⭐ 1. HTML의 URL 구조에 맞게 RequestMapping을 수정합니다.
@RequestMapping("/adminpage/productadmin")
@RequiredArgsConstructor
@Slf4j
public class AdminProductManagementController {

    private final AdminProductManagementService adminProductManagementService;
    
    // ⭐ 2. HTML의 form action, href와 정확히 일치하는 GetMapping 경로를 사용합니다.
	@GetMapping("/adminProductManagement")
    public String getProductManagementList(
            @ModelAttribute("searchCriteria") AdminProductSearchCriteria searchCriteria,
            @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
            Model model) {

        CustomerPagination<AdminProductView> pagination = adminProductManagementService.getAdminProductList(searchCriteria, currentPage);

        log.info("조회된 페이지네이션 정보: {}", pagination);

        model.addAttribute("title", "상품 승인 관리");
        model.addAttribute("pagination", pagination);
        
        return "admin/adminpage/productadmin/adminProductManagement";
    }
	
	// ⭐ 추가: 상품 반려 폼 (모달) 데이터 로딩
    @GetMapping("/rejectProductForm")
    @ResponseBody // Ajax 요청에 대한 응답으로 사용
    public Map<String, Object> rejectProductForm(@RequestParam("gdsNo") String gdsNo) {
        Map<String, Object> response = new HashMap<>();
        // 반려 사유 목록 조회
        List<ApprovalCriteria> rejectReasons = adminProductManagementService.getRejectReasonCriteriaList();
        
        response.put("gdsNo", gdsNo);
        response.put("rejectReasons", rejectReasons);
        return response;
    }

    // ⭐ 추가: 상품 반려 처리 (POST)
    @PostMapping("/rejectProduct")
    public String rejectProduct(@ModelAttribute ProductRejectRequest rejectRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        String managerId = "user_no_9"; // 임시 관리자 ID

        try {
            adminProductManagementService.rejectProductUpdate(rejectRequest, managerId);
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 반려 처리되었습니다.");
        } catch (Exception e) {
            log.error("상품 반려 처리 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "상품 반려 처리에 실패했습니다.");
        }
        return "redirect:/adminpage/productadmin/adminProductManagement";
    }
	
    // ⭐ 3. HTML의 form action과 정확히 일치하는 PostMapping 경로를 사용합니다.
	@PostMapping("/approveProduct")
	public String approveProduct(@RequestParam(name = "gdsNo") String gdsNo, HttpSession session) {
		String managerId = "user_no_9";
		adminProductManagementService.approveProductUpdate(gdsNo, managerId);
     
        // ⭐ 4. 리다이렉트 경로도 GET 요청 경로와 동일하게 수정합니다.
		return "redirect:/adminpage/productadmin/adminProductManagement";
	}
}
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductSearchCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductRejectRequest;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service.AdminProductManagementService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/productadmin")
@RequiredArgsConstructor
@Slf4j
public class AdminProductManagementController {

    private final AdminProductManagementService adminProductManagementService;
    
    // [추가] '승인/반려 기록' 페이지 조회 로직
	 // [수정] '승인/반려 기록' 페이지 조회 로직
	    @GetMapping("/approvalHistory")
	    public String getApprovalHistory(
	            @ModelAttribute("searchCriteria") AdminProductSearchCriteria searchCriteria,
	            @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
	            Model model) {
	
	        // 서비스 호출하여 페이지네이션 객체 받아오기
	        CustomerPagination<ProductApprovalHistory> pagination = adminProductManagementService.getApprovalHistoryList(searchCriteria, currentPage);
	
	        model.addAttribute("title", "승인/반려 기록");
	        model.addAttribute("pagination", pagination); // 페이지네이션 객체 전달
	        model.addAttribute("historyList", pagination.getList()); // 목록 전달
	        model.addAttribute("activeMenu", "product");
	
	        return "admin/adminpage/productadmin/approvalHistory";
	    }
    
    // [최종 버전] '전체 상품 관리' 페이지 조회 로직 (검색/페이지네이션 포함)
    @GetMapping("/allProducts")
    public String allProductsPage(
            @ModelAttribute("searchCriteria") AdminProductSearchCriteria searchCriteria,
            @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
            Model model) {

        CustomerPagination<AdminProductView> pagination = adminProductManagementService.findAllProducts(searchCriteria, currentPage);

        // ================== 이 부분을 추가해주세요 ==================
        model.addAttribute("title", "전체 상품 관리");
        // ========================================================

        model.addAttribute("pagination", pagination);
        model.addAttribute("activeMenu", "product");

        return "admin/adminpage/productadmin/allProducts";
    }
    
    // [추가] '전체 상품 관리' 페이지의 활성/비활성 상태 변경 로직
    @PostMapping("/updateStatus")
    @ResponseBody
    public Map<String, Object> updateProductStatus(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String gdsNo = payload.get("gdsNo");
            String status = payload.get("status"); // "ACTIVE" 또는 "INACTIVE"
            String managerId = "user_no_9"; 

            adminProductManagementService.updateProductStatus(gdsNo, status, managerId);

            response.put("success", true);
            response.put("message", "상품 상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            log.error("상품 상태 변경 중 오류 발생: gdsNo={}, status={}", payload.get("gdsNo"), payload.get("status"), e);
            response.put("success", false);
            response.put("message", "상태 변경 중 오류가 발생했습니다.");
        }
        return response;
    }
    
    // ================== 기존 '상품 승인 관리' 관련 코드는 그대로 유지 ==================
    
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
	
	// 상품 반려 폼 (모달) 데이터 로딩
    @GetMapping("/rejectProductForm")
    @ResponseBody
    public Map<String, Object> rejectProductForm(@RequestParam("gdsNo") String gdsNo) {
        Map<String, Object> response = new HashMap<>();
        List<ApprovalCriteria> rejectReasons = adminProductManagementService.getRejectReasonCriteriaList();
        response.put("gdsNo", gdsNo);
        response.put("rejectReasons", rejectReasons);
        log.info(">>>>>> [Controller] rejectProductForm 응답 데이터: {}", response); 
        return response;
    }

    // 상품 반려 처리 (POST)
    @PostMapping("/rejectProduct")
    public String rejectProduct(@ModelAttribute ProductRejectRequest rejectRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        String managerId = "user_no_9";
        try {
            adminProductManagementService.rejectProductUpdate(rejectRequest, managerId);
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 반려 처리되었습니다.");
        } catch (Exception e) {
            log.error("상품 반려 처리 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "상품 반려 처리에 실패했습니다.");
        }
        return "redirect:/adminpage/productadmin/adminProductManagement";
    }
	
    // 상품 승인 처리 (POST)
	@PostMapping("/approveProduct")
	public String approveProduct(@RequestParam(name = "gdsNo") String gdsNo, HttpSession session) {
		String managerId = "user_no_9";
		adminProductManagementService.approveProductUpdate(gdsNo, managerId);
		return "redirect:/adminpage/productadmin/adminProductManagement";
	}
}
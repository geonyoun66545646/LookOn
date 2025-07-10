package ks55team02.admin.adminpage.productadmin.admincategorymanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/productadmin")
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class AdminCategoryManagementController {
	
    // seller 패키지에 있는 서비스를 그대로 주입받아 사용합니다.
    private final ProductCategoryService productCategoryService;
    
 // 카테고리 활성화 처리
    @PostMapping("/activateCategory")
    public String activateCategory(@RequestParam("categoryId") String categoryId,
                                   RedirectAttributes redirectAttributes) {
        try {
            productCategoryService.activateCategory(categoryId);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 활성화되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 활성화에 실패했습니다.");
        }
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }
    
    // 카테고리 수정 폼 로딩
    @GetMapping("/updateCategory/{categoryId}")
    public String updateCategoryForm(@PathVariable("categoryId") String categoryId, Model model, RedirectAttributes redirectAttributes) {
        ProductCategory category = productCategoryService.getCategoryById(categoryId);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 카테고리입니다.");
            return "redirect:/adminpage/productadmin/adminCategoryManagement";
        }
        
        List<ProductCategory> topLevelCategories = productCategoryService.getAllTopLevelCategories();

        model.addAttribute("title", "카테고리 수정");
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("productCategory", category);
        model.addAttribute("isUpdate", true); // 수정 모드임을 알림
        
        return "admin/adminpage/productadmin/adminAddCategory"; // 등록 폼 재활용
    }

    // 카테고리 수정 처리
    @PostMapping("/updateCategory")
    public String updateCategory(
            @ModelAttribute ProductCategory productCategory,
            RedirectAttributes redirectAttributes) {
        try {
            productCategoryService.updateCategory(productCategory);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            // 외래 키 제약 조건 등으로 수정 실패 시
            redirectAttributes.addFlashAttribute("errorMessage", "수정에 실패했습니다. 이 카테고리를 참조하는 하위 카테고리나 상품이 있는지 확인해주세요.");
        }
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }

    // --- 중복 체크 API ---

    @GetMapping("/checkCategoryId")
    @ResponseBody
    public ResponseEntity<Boolean> checkCategoryId(@RequestParam("categoryId") String categoryId) {
        boolean isExists = productCategoryService.isCategoryIdExists(categoryId);
        return ResponseEntity.ok(isExists);
    }

    @GetMapping("/checkCategoryName")
    @ResponseBody
    public ResponseEntity<Boolean> checkCategoryName(@RequestParam("categoryName") String categoryName,
                                                     @RequestParam(value = "originalName", required = false) String originalName) {
        // 수정 시, 자기 자신의 이름은 중복 체크에서 제외
        if (categoryName.equals(originalName)) {
            return ResponseEntity.ok(false);
        }
        boolean isExists = productCategoryService.isCategoryNameExists(categoryName);
        return ResponseEntity.ok(isExists);
    }
    
    
    @PostMapping("/deactivateCategory")
    public String deactivateCategory(@RequestParam("categoryId") String categoryId,
                                     RedirectAttributes redirectAttributes) {
        try {
            productCategoryService.deactivateCategoryAndRelatedProducts(categoryId);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 비활성화되었습니다.");
        } catch (Exception e) {
            System.err.println("카테고리 비활성화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 비활성화에 실패했습니다.");
        }
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }
    
    // 신규 카테고리 등록 폼 페이지 요청
    @GetMapping("/addCategory")
    public String addCategoryForm(Model model) {
        
        List<ProductCategory> topLevelCategories = productCategoryService.getAllTopLevelCategories();
        
        model.addAttribute("title", "신규 카테고리 등록");
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("productCategory", new ProductCategory());
        
        // ⭐ 이 라인을 추가하여 '등록 모드'임을 명시합니다.
        model.addAttribute("isUpdate", false); 
        
        return "admin/adminpage/productadmin/adminAddCategory";
    }
    
    // 신규 카테고리 등록 처리
    @PostMapping("/addCategory")
    public String addCategory(
            @ModelAttribute ProductCategory productCategory,
            RedirectAttributes redirectAttributes) {
        
        // ⭐ 수정: 임시 관리자 ID를 설정합니다. (나중에 실제 로그인 ID로 교체)
        productCategory.setUserNo("user_no_9"); // 예시 ID

        productCategoryService.addCategory(productCategory);
        
        redirectAttributes.addFlashAttribute("message", "새로운 카테고리가 성공적으로 등록되었습니다.");
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }
    
    @GetMapping("/adminCategoryManagement")
    public String productadminCategoryController(
            @ModelAttribute SearchCriteria searchCriteria,
            Model model) {
        
        // ⭐ 추가: 검색 요청이 있었는지 확인하는 플래그
        boolean isSearch = searchCriteria.getLevels() != null || searchCriteria.getStartDate() != null ||
                           searchCriteria.getEndDate() != null || StringUtils.hasText(searchCriteria.getSearchValue());

        // ⭐ 수정: 검색 요청이 없었을 때만(최초 진입 시) 기본값을 설정
        if (!isSearch) {
            searchCriteria.setLevels(List.of(1, 2)); 
        }
        
        Map<String, Object> resultMap = productCategoryService.getCategoryList(searchCriteria);
        
        model.addAttribute("title", "상품 카테고리 관리");
        model.addAttribute("categoryList", resultMap.get("categoryList"));
        model.addAttribute("pagination", resultMap.get("pagination"));
        model.addAttribute("searchCriteria", searchCriteria);
        
        return "admin/adminpage/productadmin/adminCategoryManagement";
    } 
}
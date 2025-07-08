package ks55team02.admin.adminpage.productadmin.adminproductmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
    // 신규 카테고리 등록 폼 페이지 요청
    @GetMapping("/addCategory")
    public String addCategoryForm(Model model) {
        
        // 상위 카테고리로 선택할 수 있는 1차 카테고리 목록을 조회
        List<ProductCategory> topLevelCategories = productCategoryService.getAllTopLevelCategories();
        
        model.addAttribute("title", "신규 카테고리 등록");
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("productCategory", new ProductCategory()); // 폼 바인딩을 위한 빈 객체
        
        return "admin/adminpage/productadmin/adminAddCategory"; // 새로 만들 HTML 파일명
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
package ks55team02.admin.adminpage.productadmin.admincategorymanagement.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/productadmin")
@RequiredArgsConstructor
public class AdminCategoryManagementController {
	
    private final ProductCategoryService productCategoryService;
    
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
        model.addAttribute("isUpdate", true);
        
        return "admin/adminpage/productadmin/adminAddCategory";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(
            @ModelAttribute ProductCategory productCategory,
            @RequestParam("categoryImageFile") MultipartFile categoryImageFile,
            @RequestParam(value = "deleteCategoryImage", defaultValue = "false") boolean deleteCategoryImage,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser"); // ⭐ "loginAdmin" -> "loginUser"
        if (loginAdmin == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
            return "redirect:/adminpage/login";
        }
        
        try {
            productCategory.setMdfrNo(loginAdmin.getUserNo());
            productCategoryService.updateCategory(productCategory, categoryImageFile, deleteCategoryImage);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "수정에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }

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
        if (categoryName.equals(originalName)) {
            return ResponseEntity.ok(false);
        }
        boolean isExists = productCategoryService.isCategoryNameExists(categoryName);
        return ResponseEntity.ok(isExists);
    }
    
    @PostMapping("/deactivateCategory")
    public String deactivateCategory(@RequestParam("categoryId") String categoryId,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        
        LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser"); // ⭐ "loginAdmin" -> "loginUser"
        if (loginAdmin == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
            return "redirect:/adminpage/login"; // ⭐ "/admin/login" -> "/adminpage/login"
        }
        
        try {
            String adminId = loginAdmin.getUserNo();
            productCategoryService.deactivateCategoryAndRelatedProducts(categoryId, adminId);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 비활성화되었습니다.");
        } catch (Exception e) {
            System.err.println("카테고리 비활성화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 비활성화에 실패했습니다.");
        }
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }
    
    @GetMapping("/addCategory")
    public String addCategoryForm(Model model) {
        
        List<ProductCategory> topLevelCategories = productCategoryService.getAllTopLevelCategories();
        
        model.addAttribute("title", "신규 카테고리 등록");
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("productCategory", new ProductCategory());
        model.addAttribute("isUpdate", false); 
        
        return "admin/adminpage/productadmin/adminAddCategory";
    }
    
    @PostMapping("/addCategory")
    public String addCategory(
            @ModelAttribute ProductCategory productCategory,
            @RequestParam("categoryImageFile") MultipartFile categoryImageFile,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser"); // ⭐ "loginAdmin" -> "loginUser"
        if (loginAdmin == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
            return "redirect:/adminpage/login"; // ⭐ "/admin/login" -> "/adminpage/login"
        }
        
        productCategory.setUserNo(loginAdmin.getUserNo());

        productCategoryService.addCategory(productCategory, categoryImageFile);
        
        redirectAttributes.addFlashAttribute("message", "새로운 카테고리가 성공적으로 등록되었습니다.");
        return "redirect:/adminpage/productadmin/adminCategoryManagement";
    }
    
    @GetMapping("/adminCategoryManagement")
    public String productadminCategoryController(
            @ModelAttribute SearchCriteria searchCriteria,
            @RequestParam(value = "levels", required = false) String[] levels,
            Model model) {
        
        // ⭐ 이 부분이 수정되었습니다: 빈 문자열을 필터링하는 로직 추가
        if (levels != null) {
            List<Integer> intLevels = Arrays.stream(levels)
                                            .filter(s -> !s.isBlank()) // <--- ⭐ 이 한 줄을 추가하세요
                                            .map(Integer::parseInt)
                                            .collect(Collectors.toList());
            searchCriteria.setLevels(intLevels);
        }

        
        
        Map<String, Object> resultMap = productCategoryService.getCategoryList(searchCriteria);
        
        model.addAttribute("title", "상품 카테고리 관리");
        model.addAttribute("categoryList", resultMap.get("categoryList"));
        model.addAttribute("pagination", resultMap.get("pagination"));
        model.addAttribute("searchCriteria", searchCriteria);
        
        return "admin/adminpage/productadmin/adminCategoryManagement";
    } 
}
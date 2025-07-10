package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service;

import java.util.List;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductSearchCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductRejectRequest;
import ks55team02.util.CustomerPagination;

public interface AdminProductManagementService {
	
	// 반려 사유 목록 조회
    List<ApprovalCriteria> getRejectReasonCriteriaList();

    // 상품 반려 처리
    void rejectProductUpdate(ProductRejectRequest rejectRequest, String managerId);

    // 검색 및 페이지네이션이 적용된 상품 목록 조회
    CustomerPagination<AdminProductView> getAdminProductList(AdminProductSearchCriteria searchCriteria, int currentPage);
    
    // 상품 승인 처리 (기존과 동일)
    void approveProductUpdate(String gdsNo, String managerId);
}
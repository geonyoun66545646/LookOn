package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service;

import java.util.List;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductSearchCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductRejectRequest;
import ks55team02.util.CustomerPagination;

public interface AdminProductManagementService {
	
	// [추가] 승인/반려 기록 목록 조회 (페이지네이션 적용)
    CustomerPagination<ProductApprovalHistory> getApprovalHistoryList(AdminProductSearchCriteria searchCriteria, int currentPage);

	// [추가] 전체 상품 목록 조회 (페이지네이션 적용)
	CustomerPagination<AdminProductView> findAllProducts(AdminProductSearchCriteria searchCriteria, int currentPage);

	// [추가] 상품 상태 변경 (활성/비활성)
	void updateProductStatus(String gdsNo, String status, String managerId);

	// 반려 사유 목록 조회
	List<ApprovalCriteria> getRejectReasonCriteriaList();

	// 상품 반려 처리
	void rejectProductUpdate(ProductRejectRequest rejectRequest, String managerId);

	// 검색 및 페이지네이션이 적용된 상품 목록 조회
	CustomerPagination<AdminProductView> getAdminProductList(AdminProductSearchCriteria searchCriteria,
			int currentPage);

	// 상품 승인 처리 (기존과 동일)
	void approveProductUpdate(String gdsNo, String managerId);
}
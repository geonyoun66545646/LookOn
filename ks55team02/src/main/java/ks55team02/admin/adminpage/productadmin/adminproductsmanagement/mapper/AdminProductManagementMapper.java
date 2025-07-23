package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.mapper;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminProductManagementMapper {
	// [추가] 승인/반려 기록 개수 조회
    int countApprovalHistory(Map<String, Object> paramMap);

    // [추가] 승인/반려 기록 목록 조회
    List<ProductApprovalHistory> findApprovalHistoryList(Map<String, Object> paramMap);
	
	// [추가] 전체 상품 개수 조회 (검색 조건 포함)
    int countAllProducts(Map<String, Object> paramMap);

    // [추가] 전체 상품 목록 조회 (페이지네이션 및 검색 조건 포함)
    List<AdminProductView> findAllProductsList(Map<String, Object> paramMap);

    ProductApprovalHistory getLatestHistoryByGdsNo(String gdsNo);
    ProductApprovalHistory getLatestApprovedOrRejectedHistoryByGdsNo(String gdsNo);
    ProductApprovalHistory getLatestRejectedHistory(String gdsNo);  // 추가된 메서드
    Integer getLatestProcessedCycle(String gdsNo);  // 추가된 메서드
    int insertProductApprovalHistory(ProductApprovalHistory productApprovalHistory);
    int insertProductRejectReasonMappings(List<Map<String, Object>> mappings);
    int getLatestApprovalCycle(String gdsNo);
    List<ApprovalCriteria> getApprovalCriteriaList();
    int countAdminProducts(Map<String, Object> paramMap);
    List<AdminProductView> getAdminProductList(Map<String, Object> paramMap);
    void updateProductExposure(Map<String, Object> paramMap);
    void updateProductApprovalStatusAndCommentByHistoryCode(Map<String, Object> paramMap);
    ProductApprovalHistory getLatestPendingHistoryByGdsNo(String gdsNo);
    void updateProductApprovalHistoryStatusAndComment(ProductApprovalHistory productApprovalHistory);
}
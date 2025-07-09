package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;

@Mapper
public interface AdminProductManagementMapper {
	
	// ⭐ 새로 추가할 메소드들 (반려 기능 관련) ⭐

    // 승인/반려 기준 목록 조회 (특히 '반려' 기준)
    List<ApprovalCriteria> getApprovalCriteriaList();

    // 상품 승인 이력 삽입 (승인/반려 모두 이 메소드를 사용)
    void insertProductApprovalHistory(ProductApprovalHistory history);
    
    // 상품 반려 사유 매핑 삽입 (여러 건)
    void insertProductRejectReasonMappings(List<Map<String, Object>> mappings);
    
    // 재승인
    Integer getLatestApprovalCycle(String gdsNo);
	
    // 검색 조건에 맞는 상품 목록 개수 조회
	int countAdminProducts(Map<String, Object> params);

    // 검색 조건 및 페이지네이션을 적용한 상품 목록 조회
    List<AdminProductView> getAdminProductList(Map<String, Object> params);

    // 상품 승인 상태 업데이트 (기존과 동일)
    int updateProductApprovalStatus(Map<String, Object> params);

    // 상품 노출 여부 업데이트 (기존과 동일)
    int updateProductExposure(Map<String, Object> params);
}
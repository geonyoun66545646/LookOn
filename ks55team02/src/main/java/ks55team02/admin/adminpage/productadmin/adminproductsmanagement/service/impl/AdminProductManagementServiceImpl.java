package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductSearchCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.AdminProductView;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ApprovalCriteria;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductRejectRequest;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.mapper.AdminProductManagementMapper;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.service.AdminProductManagementService;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminProductManagementServiceImpl implements AdminProductManagementService {

    private final AdminProductManagementMapper adminProductManagementMapper;
    private final ProductsMapper productsMapper; // ⭐ ProductsMapper 주입
    private static final int PAGE_SIZE = 10; // 한 페이지에 보여줄 아이템 수
    private static final int BLOCK_SIZE = 5;  // 페이지네이션 블록 크기
    
    // [추가] 승인/반려 기록 목록 조회 로직 구현
    @Override
    public CustomerPagination<ProductApprovalHistory> getApprovalHistoryList(AdminProductSearchCriteria searchCriteria, int currentPage) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchCriteria", searchCriteria);

        // (주의: Mapper에 countApprovalHistory 메소드 추가 필요)
        int totalCount = adminProductManagementMapper.countApprovalHistory(params);
        log.info(">>>>>> [Service] '승인/반려 기록' 조회된 기록 수: {}", totalCount);

        if (totalCount == 0) {
            return new CustomerPagination<>(Collections.emptyList(), 0, currentPage, PAGE_SIZE, BLOCK_SIZE);
        }

        int offset = (currentPage - 1) * PAGE_SIZE;
        params.put("limit", PAGE_SIZE);
        params.put("offset", offset);
        
        // (주의: Mapper에 findApprovalHistoryList 메소드 추가 필요)
        List<ProductApprovalHistory> historyList = adminProductManagementMapper.findApprovalHistoryList(params);
        
        return new CustomerPagination<>(historyList, totalCount, currentPage, PAGE_SIZE, BLOCK_SIZE);
    }
    
    // [추가] 전체 상품 목록 조회 로직
    @Override
    public CustomerPagination<AdminProductView> findAllProducts(AdminProductSearchCriteria searchCriteria, int currentPage) {
        Map<String, Object> params = new HashMap<>();
        params.put("searchCriteria", searchCriteria);

        // [수정] countAdminProducts 대신 countAllProducts를 호출하도록 변경
        int totalCount = adminProductManagementMapper.countAllProducts(params);
        log.info(">>>>>> [Service] '전체 상품 관리' 조회된 상품 수: {}", totalCount);

        if (totalCount == 0) {
            return new CustomerPagination<>(Collections.emptyList(), 0, currentPage, PAGE_SIZE, BLOCK_SIZE);
        }

        int offset = (currentPage - 1) * PAGE_SIZE;
        params.put("limit", PAGE_SIZE);
        params.put("offset", offset);

        // 목록 조회 쿼리는 이미 올바르게 findAllProductsList를 호출하고 있습니다.
        List<AdminProductView> productList = adminProductManagementMapper.findAllProductsList(params);
        
        return new CustomerPagination<>(productList, totalCount, currentPage, PAGE_SIZE, BLOCK_SIZE);
    }
    
    // [추가] 상품 상태 변경 로직
    @Override
    @Transactional
    public void updateProductStatus(String gdsNo, String status, String managerId) {
        boolean isExposure = "ACTIVE".equalsIgnoreCase(status);
        
        Map<String, Object> params = new HashMap<>();
        params.put("gdsNo", gdsNo);
        params.put("exposure", isExposure);
        params.put("managerId", managerId);
        
        // (주의: Mapper의 updateProductExposure 메소드가 이 파라미터들을 처리해야 함)
        adminProductManagementMapper.updateProductExposure(params);
        log.info(">>>>>> [Service] 상품 상태 변경: gdsNo={}, exposure={}, managerId={}", gdsNo, isExposure, managerId);
    }
    
    @Override
    public CustomerPagination<AdminProductView> getAdminProductList(AdminProductSearchCriteria searchCriteria, int currentPage) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("searchCriteria", searchCriteria);

        int totalCount = adminProductManagementMapper.countAdminProducts(params);

        // ========================================================
        // ⭐ 3. 여기에 아래 로그 한 줄을 추가해주세요.
        log.info(">>>>>> [Service] 조회된 전체 상품 수 (totalCount): {}", totalCount);
        // ========================================================

        if (totalCount == 0) {
            return new CustomerPagination<>(Collections.emptyList(), 0, currentPage, PAGE_SIZE, BLOCK_SIZE);
        }

        int offset = (currentPage - 1) * PAGE_SIZE;
        params.put("limit", PAGE_SIZE);
        params.put("offset", offset);

        List<AdminProductView> productList = adminProductManagementMapper.getAdminProductList(params);

        // ========================================================
        // ⭐ 4. 여기에 아래 로그 한 줄을 추가해주세요.
        log.info(">>>>>> [Service] 조회된 상품 리스트 크기 (productList.size()): {}", productList.size());
        // ========================================================
        
        return new CustomerPagination<>(productList, totalCount, currentPage, PAGE_SIZE, BLOCK_SIZE);
    }
    
    
	/*반려 관리*/
    @Override
    public List<ApprovalCriteria> getRejectReasonCriteriaList() {
        return adminProductManagementMapper.getApprovalCriteriaList();
    }

    @Override
    @Transactional
    public void rejectProductUpdate(ProductRejectRequest rejectRequest, String managerId) {
        String gdsNo = rejectRequest.getGdsNo();

        // 1. 새로운 승인 이력 PK 생성 (현재 코드와 동일)
        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();

        // 2. product_approval_history에 '반려' 상태 이력 추가
        ProductApprovalHistory history = new ProductApprovalHistory();
        history.setAprvRjctHstryCd(newHistoryCode);
        history.setGdsNo(gdsNo);
        history.setPrcsMngrId(managerId);
        history.setAprvSttsCd("반려"); // 상태를 '반려'로 설정

        // ⭐ ⭐ ⭐ 이 부분 수정: 가장 최근의 '대기' 상태 이력의 차수를 가져옵니다. ⭐ ⭐ ⭐
        // 이력을 조회할 때 '대기' 상태의 이력 중 가장 최신 것을 찾아 그 차수를 사용합니다.
        // 이를 위해 AdminProductManagementMapper에 새로운 메서드가 필요할 수 있습니다.
        // 예: getLatestPendingApprovalCycle(gdsNo)
        // 일단은 getLatestApprovalCycle을 사용하지만, 실제로는 대기 상태의 차수를 찾아야 합니다.
        // 여기서는 기존에 계산된 차수가 넘어왔다고 가정하고 그대로 사용합니다.
        // 기존 코드처럼 latestCycle + 1이 아니라, 현재 대기 중인 차수를 그대로 가져와야 합니다.
        // (만약 updateProduct에서 newCycle이 올바르게 설정되었다면, 그 값을 사용합니다.)

        // ⭐ 변경 제안: 가장 최신 이력 (어떤 상태든)의 차수를 가져옵니다.
        // 판매자가 대기 상태로 올린 그 차수를 그대로 사용해야 합니다.
        Integer latestCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo);
        int currentCycle = (latestCycle != null) ? latestCycle : 1; // 대기 상태의 차수 사용
        history.setAprvRjctCycl(currentCycle); // 현재 대기 중인 차수와 동일하게 설정

        // 관리자 직접 입력 상세 내용 설정 (현재 코드와 동일)
        history.setMngrCmntCn(rejectRequest.getManagerComment());

        // 선택된 반려 사유 코드를 기반으로 rjct_rsn (요약 사유) 설정 (현재 코드와 동일)
        if (rejectRequest.getRejectReasonCodes() != null && !rejectRequest.getRejectReasonCodes().isEmpty()) {
            List<ApprovalCriteria> allRejectCriteria = adminProductManagementMapper.getApprovalCriteriaList(); // 모든 반려 기준 조회
            String summarizedReason = rejectRequest.getRejectReasonCodes().stream()
                .map(code -> allRejectCriteria.stream()
                              .filter(criteria -> criteria.getAprvRjctRsnCd().equals(code))
                              .map(ApprovalCriteria::getAprvRjctDtlCn)
                              .findFirst()
                              .orElse(""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
            
            history.setRjctRsn(summarizedReason);
        }

        adminProductManagementMapper.insertProductApprovalHistory(history);

        // 3. product_rjct_rsn_hstry_mapping에 선택된 반려 사유 매핑 기록 (현재 코드와 동일)
        if (rejectRequest.getRejectReasonCodes() != null && !rejectRequest.getRejectReasonCodes().isEmpty()) {
            List<Map<String, Object>> mappings = new ArrayList<>();
            for (String reasonCode : rejectRequest.getRejectReasonCodes()) {
                Map<String, Object> mapping = new HashMap<>();
                mapping.put("aprvRjctHstryCd", newHistoryCode);
                mapping.put("aprvRjctRsnCd", reasonCode);
                mapping.put("creatrNo", managerId);
                mappings.add(mapping);
            }
            adminProductManagementMapper.insertProductRejectReasonMappings(mappings);
        }

        // 4. products 테이블의 expsr_yn을 false로 변경 (노출 비활성화) (현재 코드와 동일)
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("gdsNo", gdsNo);
        productParams.put("managerId", managerId);
        productParams.put("exposure", false);
        adminProductManagementMapper.updateProductExposure(productParams);
    }

    
	/*승인 관리 */
    @Override
    @Transactional
    public void approveProductUpdate(String gdsNo, String managerId) {
        // 새 이력 코드 생성 (현재 코드와 동일)
        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();
        
        // ⭐ ⭐ ⭐ 이 부분 수정: 가장 최근의 '대기' 상태 이력의 차수를 가져옵니다. ⭐ ⭐ ⭐
        // 반려와 마찬가지로, 현재 대기 중인 이력의 차수를 그대로 사용해야 합니다.
        Integer latestCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo);
        int currentCycle = (latestCycle != null) ? latestCycle : 1; // 대기 상태의 차수 사용

        // ProductApprovalHistory 객체 생성 (현재 코드와 동일)
        ProductApprovalHistory history = new ProductApprovalHistory();
        history.setAprvRjctHstryCd(newHistoryCode);
        history.setGdsNo(gdsNo);
        history.setPrcsMngrId(managerId);
        history.setAprvSttsCd("승인");
        history.setPrcsDt(LocalDateTime.now());
        history.setAprvRjctCycl(currentCycle); // 현재 대기 중인 차수와 동일하게 설정
        history.setMngrCmntCn("관리자에 의해 승인 처리되었습니다.");

        adminProductManagementMapper.insertProductApprovalHistory(history);

        // 2. products 테이블의 노출여부(expsr_yn)를 true로 변경 (현재 코드와 동일)
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("gdsNo", gdsNo);
        productParams.put("managerId", managerId);
        productParams.put("exposure", true);
        adminProductManagementMapper.updateProductExposure(productParams);
    }
}
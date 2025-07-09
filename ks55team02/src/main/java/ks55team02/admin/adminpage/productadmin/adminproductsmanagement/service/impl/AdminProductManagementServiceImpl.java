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

        // 1. 새로운 승인 이력 PK 생성
        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();

        // 2. product_approval_history에 '반려' 상태 이력 추가
        ProductApprovalHistory history = new ProductApprovalHistory();
        history.setAprvRjctHstryCd(newHistoryCode);
        history.setGdsNo(gdsNo);
        history.setPrcsMngrId(managerId);
        history.setAprvSttsCd("반려"); // 상태를 '반려'로 설정

        // 기존 상품의 최신 승인 차수 가져오기 (+1)
        Integer latestCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo);
        int newCycle = (latestCycle != null) ? latestCycle + 1 : 1;
        history.setAprvRjctCycl(newCycle);

        // 관리자 직접 입력 상세 내용 설정
        history.setMngrCmntCn(rejectRequest.getManagerComment());

        // 선택된 반려 사유 코드를 기반으로 rjct_rsn (요약 사유) 설정
        // ApprovalCriteria 목록에서 해당 코드를 찾아 상세 내용을 가져옴
        if (rejectRequest.getRejectReasonCodes() != null && !rejectRequest.getRejectReasonCodes().isEmpty()) {
            List<ApprovalCriteria> allRejectCriteria = adminProductManagementMapper.getApprovalCriteriaList(); // 모든 반려 기준 조회
            String summarizedReason = rejectRequest.getRejectReasonCodes().stream()
                .map(code -> allRejectCriteria.stream()
                              .filter(criteria -> criteria.getAprvRjctRsnCd().equals(code))
                              .map(ApprovalCriteria::getAprvRjctDtlCn)
                              .findFirst()
                              .orElse("")) // 매핑되는 사유가 없으면 빈 문자열
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", ")); // 쉼표로 연결
            
            history.setRjctRsn(summarizedReason);
        }

        adminProductManagementMapper.insertProductApprovalHistory(history);

        // 3. product_rjct_rsn_hstry_mapping에 선택된 반려 사유 매핑 기록
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

        // 4. products 테이블의 expsr_yn을 false로 변경 (노출 비활성화)
        // actvtn_yn은 true 유지 (판매자에게는 계속 보임)
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("gdsNo", gdsNo);
        productParams.put("managerId", managerId); // mdfr_no 업데이트에 사용
        productParams.put("exposure", false); // expsr_yn = false
        // 기존 updateProductExposure 메소드 재사용
        adminProductManagementMapper.updateProductExposure(productParams);
    }

    
	/*승인 관리 */
    @Override
    @Transactional
    public void approveProductUpdate(String gdsNo, String managerId) {
        // 1. product_approval_history 상태를 '승인'으로 변경
        // 승인도 새로운 이력으로 남기는 것이 이력 관리에 더 적합합니다.
        // 기존 updateProductApprovalStatus는 상태만 변경하고 새로운 이력은 남기지 않으므로,
        // insertProductApprovalHistory를 사용하는 것이 좋습니다.
        
        // 새 이력 코드 생성
        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();
        
        // 기존 상품의 최신 승인 차수 가져오기 (+1)
        Integer latestCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo);
        int newCycle = (latestCycle != null) ? latestCycle + 1 : 1;

        // ProductApprovalHistory 객체 생성
        ProductApprovalHistory history = new ProductApprovalHistory();
        history.setAprvRjctHstryCd(newHistoryCode);
        history.setGdsNo(gdsNo);
        history.setPrcsMngrId(managerId);
        history.setAprvSttsCd("승인");
        history.setPrcsDt(LocalDateTime.now());
        history.setAprvRjctCycl(newCycle);
        history.setMngrCmntCn("관리자에 의해 승인 처리되었습니다.");

        adminProductManagementMapper.insertProductApprovalHistory(history);

        // 2. products 테이블의 노출여부(expsr_yn)를 true로 변경
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("gdsNo", gdsNo);
        productParams.put("managerId", managerId);
        productParams.put("exposure", true);
        adminProductManagementMapper.updateProductExposure(productParams);
    }
}
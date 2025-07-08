package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.Pagination;    // Pagination 임포트 추가
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 임포트 추가

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
public class SettlementDetailByStoreController {
	
	private final StoreSettlementService storeSettlementService;

    /**
     * 정산 관리 페이지를 로드하고 정산 대상 상점 목록을 표시합니다.
     * 페이지네이션 및 검색 기능을 포함합니다.
     * @param currentPage 현재 페이지 번호 (기본값 1)
     * @param pageSize 페이지당 레코드 수 (기본값 10)
     * @param model Model 객체
     * @return 정산 관리 뷰 이름
     */
	@GetMapping("/settlementDetailByStore")
	public String settlementManagement(
            @RequestParam(name = "currentPage", defaultValue = "1", required = false) int currentPage,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
			Model model) {

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setCurrentPage(currentPage);
        searchCriteria.setPageSize(pageSize);

        // 전체 데이터 개수 조회
        int totalCount = storeSettlementService.getTotalSettlementCount();
        
        // 페이지네이션 객체 생성 (사용자 제공 Pagination.java 생성자 사용)
        Pagination pagination = new Pagination(totalCount, searchCriteria);

        // SearchCriteria의 offset은 Pagination 생성자 내부에서 계산되므로 별도로 설정할 필요 없음.
        // 하지만 매퍼에 전달하기 위해 SearchCriteria 객체에 설정된 offset을 사용해야 합니다.
        // Pagination 생성자에서 searchCriteria.setOffset(this.limitStart); 가 호출되므로,
        // searchCriteria 객체는 이미 올바른 offset 값을 가지고 있습니다.

        // 현재 페이지에 해당하는 정산 목록 조회
        List<StoreSettlementListViewDTO> settlementList = storeSettlementService.getAllStoreSettlementsForList(searchCriteria);
        
        // Model에 데이터 추가
        model.addAttribute("settlementList", settlementList);
        model.addAttribute("totalCount", totalCount); // 총 데이터 수
        model.addAttribute("pagination", pagination); // 페이지네이션 정보
		
		// HTML 템플릿 경로
		return "admin/adminpage/storeadmin/settlementDetailByStore"; 
	}

    /**
     * 특정 정산 건을 '판매정산완료' 상태로 처리하는 API 엔드포인트
     * POST /admin/settlement/api/process-single
     * @param payload 요청 본문 (storeClclnId 포함)
     * @return 처리 결과 메시지
     */
    @PostMapping("/api/process-single")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processSingleSettlement(@RequestBody Map<String, String> payload) {
        String storeClclnId = payload.get("storeClclnId");
        if (storeClclnId == null || storeClclnId.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "정산 ID가 누락되었습니다."), HttpStatus.BAD_REQUEST);
        }

        boolean success = storeSettlementService.completeSettlement(storeClclnId);

        if (success) {
            return new ResponseEntity<>(Map.of("message", "선택된 정산이 성공적으로 완료되었습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "정산 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 여러 정산 건을 일괄 '판매정산완료' 상태로 처리하는 API 엔드포인트
     * POST /admin/settlement/api/process-batch
     * @param payload 요청 본문 (storeClclnIds 목록 포함)
     * @return 처리 결과 메시지
     */
    @PostMapping("/api/process-batch")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processBatchSettlements(@RequestBody Map<String, List<String>> payload) {
        List<String> storeClclnIds = payload.get("storeClclnIds");
        if (storeClclnIds == null || storeClclnIds.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "정산 ID 목록이 누락되었습니다."), HttpStatus.BAD_REQUEST);
        }

        boolean success = storeSettlementService.completeBatchSettlements(storeClclnIds);

        if (success) {
            return new ResponseEntity<>(Map.of("message", storeClclnIds.size() + "개의 정산이 성공적으로 완료되었습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "일괄 정산 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 새로운 정산 대기 항목을 생성하는 API 엔드포인트 (외부 시스템 연동용)
     * POST /admin/settlement/api/create-pending
     * @param payload 요청 본문 (storeId, totSelAmt, selFeeRt, plcyId 포함)
     * @return 생성 결과 메시지
     */
    @PostMapping("/api/create-pending")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createPendingSettlement(@RequestBody Map<String, Object> payload) {
        try {
            String storeId = (String) payload.get("storeId");
            BigDecimal totSelAmt = new BigDecimal(payload.get("totSelAmt").toString());
            BigDecimal selFeeRt = new BigDecimal(payload.get("selFeeRt").toString());
            String plcyId = (String) payload.get("plcyId");

            boolean success = storeSettlementService.createPendingSettlement(storeId, totSelAmt, selFeeRt, plcyId);

            if (success) {
                return new ResponseEntity<>(Map.of("message", "정산 대기 항목 생성 성공"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("message", "정산 대기 항목 생성 실패"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("정산 대기 항목 생성 API 오류: " + e.getMessage());
            return new ResponseEntity<>(Map.of("message", "서버 오류: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 특정 상점의 계좌 정보를 조회하는 API 엔드포인트
     * GET /admin/settlement/api/account-details/{storeId}
     * @param storeId 조회할 상점 ID
     * @return 해당 상점의 계좌 정보 (JSON)
     */
    @GetMapping("/api/account-details/{storeId}")
    @ResponseBody
    public ResponseEntity<StoreAccountDTO> getAccountDetails(@PathVariable String storeId) {
        try {
            StoreAccountDTO account = storeSettlementService.getStoreAccountDetailsByStoreId(storeId);
            if (account != null) {
                return new ResponseEntity<>(account, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("계좌 정보 조회 API 오류: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 상점의 정산 내역을 조회하는 API 엔드포인트 (AJAX 호출용)
     * GET /admin/settlement/api/history/{storeId}
     * @param storeId 조회할 상점 ID
     * @return 해당 상점의 정산 내역 목록
     */
    @GetMapping("/api/history/{storeId}")
    @ResponseBody // JSON 응답을 위해 사용
    public ResponseEntity<List<StoreSettlementDTO>> getSettlementHistory(@PathVariable String storeId) {
        try {
            List<StoreSettlementDTO> history = storeSettlementService.getSettlementHistoryByStoreId(storeId);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("정산 내역 조회 API 오류: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
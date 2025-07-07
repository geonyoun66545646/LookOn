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
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
public class SettlementDetailByStoreController {
	
	private final StoreSettlementService storeSettlementService;


//	@GetMapping("/settlementDetailByStore")
//	public String storeadminsettlementController() {
//		
//		return "admin/adminpage/storeadmin/settlementDetailByStore";
//	}
	
	/**
     * 정산 관리 페이지를 표시하고, 정산 대상 상점 목록을 가져옵니다.
     * GET /admin/settlement
     * @param model Thymeleaf로 데이터를 전달하기 위한 Model 객체
     * @return 정산 관리 페이지 템플릿 경로
     */
    @GetMapping("/settlementDetailByStore")
    public String settlementManagement(Model model) {
        List<StoreSettlementListViewDTO> settlementList = storeSettlementService.getAllStoreSettlementsForList();
        model.addAttribute("settlementList", settlementList);
        return "admin/adminpage/storeadmin/settlementDetailByStore";// HTML 파일 경로
    }

    /**
     * 특정 상점을 정산 처리하는 API 엔드포인트 (AJAX 호출용)
     * POST /admin/settlement/api/process
     * @param payload 요청 본문 (storeId, totSelAmt, selFeeRt, clclnAmt, actnoId 포함)
     * @return 성공/실패 메시지
     */
    @PostMapping("/api/process")
    @ResponseBody // JSON 응답을 위해 사용
    public ResponseEntity<String> processSettlement(@RequestBody Map<String, Object> payload) {
        try {
            String storeId = (String) payload.get("storeId");
            // BigDecimal로 형변환 시 주의: Double로 넘어올 수 있으므로 String으로 받아서 변환
            BigDecimal totSelAmt = new BigDecimal(payload.get("totSelAmt").toString());
            BigDecimal selFeeRt = new BigDecimal(payload.get("selFeeRt").toString());
            BigDecimal clclnAmt = new BigDecimal(payload.get("clclnAmt").toString());
            String actnoId = (String) payload.get("actnoId"); // 실제 계좌 ID는 DB에서 가져와야 함

            boolean success = storeSettlementService.processSettlement(storeId, totSelAmt, selFeeRt, clclnAmt, actnoId);

            if (success) {
                return new ResponseEntity<>("{\"message\": \"정산 처리 성공\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\": \"정산 처리 실패\"}", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("정산 처리 API 오류: " + e.getMessage());
            return new ResponseEntity<>("{\"message\": \"서버 오류: " + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
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

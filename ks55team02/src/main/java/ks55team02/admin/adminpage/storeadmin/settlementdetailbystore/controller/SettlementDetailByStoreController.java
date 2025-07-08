package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Model 임포트
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // @ModelAttribute 임포트
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.Pagination; // Pagination 임포트
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 임포트

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
public class SettlementDetailByStoreController {

    private final StoreSettlementService storeSettlementService;

    @GetMapping("/settlementDetailByStore")
    public String storeadminSettlementController(
            @ModelAttribute SearchCriteria searchCriteria, // SearchCriteria를 모델 어트리뷰트로 받음
            Model model
    ) {
        // 1. 전체 정산 정보 개수 조회 (검색 조건 적용)
        int totalRecordCount = storeSettlementService.getTotalSettlementCount(searchCriteria);

        // 2. Pagination 객체 생성 및 페이지 정보 계산
        Pagination pagination = new Pagination(totalRecordCount, searchCriteria);

        // 3. 페이지네이션 및 검색 조건이 적용된 정산 목록 조회
        List<StoreSettlementListViewDTO> settlementList = storeSettlementService.getAllStoreSettlementsForList(searchCriteria);

        // 4. Model에 데이터 추가
        model.addAttribute("title", "스토어별 정산 내역");
        model.addAttribute("settlementList", settlementList);
        model.addAttribute("pagination", pagination); // 페이지네이션 정보
        model.addAttribute("searchCriteria", searchCriteria); // 검색 조건 유지

        return "admin/adminpage/storeadmin/settlementDetailByStore";
    }

    // 다른 매핑 추가 (예: POST 요청 처리, 상세 보기, 정산 처리 등)
}
package ks55team02.admin.adminpage.storeadmin.appadmin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.appadmin.service.AppAdminService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.common.domain.store.AppStore;
import ks55team02.common.enums.ApplyStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
@Log4j2
public class AppAdminController {
	
	private final AppAdminService appAdminService;

	@GetMapping("/appAdmin")
	public String appAdmin(
	        Model model,
	        AppStore appAdmin // SearchCriteria를 상속받으므로 currentPage, pageSize, searchKey, searchValue 등을 바로 받을 수 있음
	) {
		log.info("컨트롤러: appAdmin 호출 - 현재 페이지: {}, 페이지 크기: {}, 검색 키: {}, 검색 값: {}",
	             appAdmin.getCurrentPage(), appAdmin.getPageSize(), appAdmin.getSearchKey(), appAdmin.getSearchValue());
		
		 // 만약 화면에서 넘어온 필터 조건이 없다면, 기본 필터(모든 상태)를 설정합니다.
		if (appAdmin.getFilterConditions() == null || appAdmin.getFilterConditions().isEmpty()) {
            List<String> defaultFilters = new ArrayList<>();
            // Enum의 모든 코드 값을 리스트에 추가
            for (ApplyStatus status : ApplyStatus.values()) {
                defaultFilters.add(status.getCode());
            }
            appAdmin.setFilterConditions(defaultFilters);
            log.info("기본 필터 조건 설정: {}", defaultFilters);
        }

		// 1. 전체 개수 조회
		// AppAdmin 객체(SearchCriteria 상속)를 사용하여 검색 조건에 맞는 전체 레코드 수를 가져옵니다.
		int totalRecordCount = appAdminService.getAppAdminCount(appAdmin); // appAdminService에 AppAdmin 객체를 전달하여 검색 조건 반영
		
		// 2. Pagination 객체 생성 및 페이지네이션 정보 계산
		// totalRecordCount와 현재 검색/페이지네이션 기준(appAdmin)을 기반으로 페이지네이션 정보를 계산합니다.
		Pagination pagination = new Pagination(totalRecordCount, appAdmin);
		log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
	             pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());
		
		// 3. 목록 조회 시 LIMIT, OFFSET 값 설정을 위해 AppAdmin 객체에 pagination 정보 주입
		appAdmin.setOffset(pagination.getLimitStart()); // AppAdmin은 SearchCriteria를 상속하므로 offset 필드를 가짐
		// appAdmin.setPageSize(pagination.getRecordSize()); // 이 값은 이미 생성자에서 AppAdmin의 pageSize를 사용했으므로 다시 설정할 필요 없음.
		                                                // 만약 request parameter로 pageSize가 넘어오지 않았다면 기본값(10)이 사용됨.
		
		// 4. 상점 신청(어드민페이지) 목록 조회 (페이지네이션 및 검색 조건 적용)
		List<AppStore> appAdminList = appAdminService.getAppAdminList(appAdmin, pagination.getLimitStart(), appAdmin.getPageSize());
		log.info("컨트롤러: appAdminList 조회 결과 개수: {}", appAdminList.size());
		
		model.addAttribute("title", "상점 신청");
		model.addAttribute("appAdminList", appAdminList);
		model.addAttribute("pagination", pagination); // View에서 페이지네이션 UI를 렌더링하기 위해 Pagination 객체 전달
		model.addAttribute("searchCriteria", appAdmin); // View에서 검색 조건 유지 및 폼 데이터 바인딩을 위해 전달
		
		return "admin/adminpage/storeadmin/appAdminView";
	}
	
	@GetMapping("/appDetail")
	public String getAppDetail(@RequestParam("aplyId") String aplyId, Model model) {
	    log.info("컨트롤러: getAppDetail 호출 - aplyId: {}", aplyId);

	    AppStore appAdmin = appAdminService.getAppAdminById(aplyId);

	    log.info("컨트롤러: appAdminService.getAppAdminById 결과: {}", appAdmin);

	    if (appAdmin != null) {
	        model.addAttribute("title", "상점 신청 상세");
	        model.addAttribute("appStore", appAdmin);
	        log.info("컨트롤러: model에 appAdmin 추가 완료");
	        return "admin/adminpage/storeadmin/appDetailView"; // 데이터가 있을 때만 상세 뷰로
	    } else {
	        log.warn("해당 aplyId로 조회된 AppAdmin 데이터가 없습니다: {}", aplyId);
	        model.addAttribute("errorMessage", "신청 정보를 찾을 수 없습니다.");
	        return "error/dataNotFound"; // 데이터가 없을 때 에러 뷰로 리다이렉트
	    }
	}
	
	/**
     * 상점 신청 상태를 업데이트하는 AJAX 핸들러
     * @param payload aplyId, aplyStts, aprvRjctRsn을 담은 JSON 데이터
     * @return 처리 결과 메시지
     */
    @PostMapping("/updateStatus")
    @ResponseBody // 이 메소드는 뷰가 아닌, 데이터(JSON)를 반환함을 의미
    public Map<String, String> updateStatus(@RequestBody AppStore appStore) {
        log.info("컨트롤러: updateStatus 호출 - 데이터: {}", appStore);
        try {
            appAdminService.updateAppStatus(appStore);
            return Map.of("status", "success", "message", "상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("상태 업데이트 실패", e);
            return Map.of("status", "error", "message", "업데이트 중 오류가 발생했습니다.");
        }
    }
	
}
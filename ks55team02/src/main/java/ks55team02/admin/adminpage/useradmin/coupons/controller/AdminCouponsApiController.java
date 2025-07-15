// ks55team02.admin.adminpage.useradmin.coupons.controller.AdminCouponsApiController.java

package ks55team02.admin.adminpage.useradmin.coupons.controller;

import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 추가

import java.util.Map;

@RestController
@RequestMapping("/adminpage/useradmin/api") // 기본 요청 경로: /adminpage/useradmin/api
@RequiredArgsConstructor
public class AdminCouponsApiController {

	private final AdminCouponsService adminCouponsService;

	/**
	 * 쿠폰 목록 데이터 API (AJAX + 페이지네이션 적용)
	 */
	@GetMapping("/coupons") // 최종 경로: /adminpage/useradmin/api/coupons
	// 1. @ModelAttribute로 SearchCriteria를 파라미터로 받습니다.
	// 2. @RequestParam으로 'page' 파라미터를 명시적으로 받아서 SearchCriteria에 설정해주는 것이 좋습니다.
	public Map<String, Object> getCouponsListApi(
	    @RequestParam(value = "page", defaultValue = "1") int page, // 이 부분을 추가하는 것을 권장합니다.
	    @ModelAttribute SearchCriteria searchCriteria) {
		
		// 3. 프론트엔드에서 넘어온 페이지 번호를 SearchCriteria에 명시적으로 설정
		searchCriteria.setCurrentPage(page); 

		// 4. Service에 searchCriteria를 전달하고, 목록과 페이지 정보가 담긴 Map을 반환받습니다.
		return adminCouponsService.getCouponsList(searchCriteria);
	}
}
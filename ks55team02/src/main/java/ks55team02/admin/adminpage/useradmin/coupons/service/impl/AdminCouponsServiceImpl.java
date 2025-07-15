package ks55team02.admin.adminpage.useradmin.coupons.service.impl;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.mapper.AdminCouponsMapper;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCouponsServiceImpl implements AdminCouponsService {

	private final AdminCouponsMapper adminCouponsMapper;

	// [추가] 쿠폰 정보 수정
	@Override
	public void updateCoupon(AdminCoupons adminCoupons) {
		adminCouponsMapper.updateCoupon(adminCoupons);
	}

	@Override
	public void addCoupon(AdminCoupons adminCoupons) {

		// 1. DB에서 마지막 쿠폰 ID를 가져옵니다.
		String lastCouponId = adminCouponsMapper.getLastCouponId();

		int newCouponNum = 1;
		// 2. (수정) 마지막 쿠폰 ID가 null이 아닐 때만 다음 번호를 계산합니다.
		if (lastCouponId != null) {
			// "PBLCPN_" 접두사 제거 -> "020"
			String numericPart = lastCouponId.replace("PBLCPN_", "");
			// 문자열을 숫자로 변환 (020 -> 20) 하고 1을 더함 (-> 21)
			newCouponNum = Integer.parseInt(numericPart) + 1;
		}
		// 만약 마지막 쿠폰 ID가 null이면, newCouponNum은 초기값인 1이 그대로 사용됩니다.

		// 3. 다음 쿠폰 ID를 생성합니다. (예: "PBLCPN_001")
		String nextCouponId = String.format("PBLCPN_%03d", newCouponNum);

		// 4. 생성된 새 ID를 쿠폰 객체에 설정합니다.
		adminCoupons.setPblcnCpnId(nextCouponId);

		// 5. ID가 설정된 쿠폰 객체를 DB에 INSERT 합니다.
		adminCouponsMapper.addCoupon(adminCoupons);
	}

	@Override
	public Map<String, Object> getCouponsList(SearchCriteria searchCriteria) {
		// ⭐⭐ 추가할 디버깅 로그 ⭐⭐
		System.out.println("DEBUG_SERVICE: getCouponsList 호출 - 현재 페이지 (searchCriteria.currentPage): "
				+ searchCriteria.getCurrentPage());
		System.out.println(
				"DEBUG_SERVICE: getCouponsList 호출 - 페이지 크기 (searchCriteria.pageSize): " + searchCriteria.getPageSize());

		int couponsCount = adminCouponsMapper.getCouponsCount(searchCriteria);
		System.out.println("DEBUG_SERVICE: getCouponsCount 결과 (전체 쿠폰 수): " + couponsCount);

		Pagination pagination = new Pagination(couponsCount, searchCriteria); // searchCriteria를 Pagination 생성자에 넘김
		System.out.println("DEBUG_SERVICE: Pagination 객체 생성 후 (totalPageCount): " + pagination.getTotalPageCount());
		System.out.println("DEBUG_SERVICE: Pagination 객체 생성 후 (startPage): " + pagination.getStartPage()); // 시작 페이지 확인
		System.out.println("DEBUG_SERVICE: Pagination 객체 생성 후 (existPrevBlock): " + pagination.isExistPrevBlock()); // ⭐⭐
																													// 이
																													// 값이
																													// 2페이지에서
																													// true가
																													// 되는지
																													// 확인
		System.out.println("DEBUG_SERVICE: Pagination 객체 생성 후 (existNextBlock): " + pagination.isExistNextBlock());

		// 페이징 처리를 위한 offset 계산 및 설정
		searchCriteria.setOffset(pagination.getLimitStart()); // Pagination 객체의 계산 결과를 활용

		List<AdminCoupons> couponsList = adminCouponsMapper.getCouponsList(searchCriteria);
		System.out.println("DEBUG_SERVICE: 조회된 쿠폰 리스트 크기: " + (couponsList != null ? couponsList.size() : 0));

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("pagination", pagination);
		resultMap.put("couponsList", couponsList);
		resultMap.put("totalCount", couponsCount);

		return resultMap;
	}

	// [추가/확인] ID로 특정 쿠폰 정보 조회
	@Override
	public AdminCoupons getCouponById(String pblcnCpnId) {
		return adminCouponsMapper.getCouponById(pblcnCpnId);
	}

	// [추가] 쿠폰 삭제
	@Override
	public void deleteCoupon(String pblcnCpnId) {
		adminCouponsMapper.deleteCoupon(pblcnCpnId);
	}
}
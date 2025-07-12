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
	public List<AdminCoupons> getCouponsList() {
		// Mapper를 통해 모든 쿠폰 데이터 조회
		return adminCouponsMapper.getCouponsList();
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
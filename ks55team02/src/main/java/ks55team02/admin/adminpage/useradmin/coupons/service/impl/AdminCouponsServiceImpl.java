package ks55team02.admin.adminpage.useradmin.coupons.service.impl;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import ks55team02.admin.adminpage.useradmin.coupons.mapper.AdminCouponsMapper;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import ks55team02.admin.common.domain.Pagination;
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

    // ... addCoupon, updateCoupon 등 다른 메소드들 ...
    @Override
	public void updateCoupon(AdminCoupons adminCoupons) {
		adminCouponsMapper.updateCoupon(adminCoupons);
	}

	@Override
	public void addCoupon(AdminCoupons adminCoupons) {
		String lastCouponId = adminCouponsMapper.getLastCouponId();
		int newCouponNum = 1;
		if (lastCouponId != null && !lastCouponId.isEmpty()) {
			String numericPart = lastCouponId.replace("PBLCPN_", "");
			newCouponNum = Integer.parseInt(numericPart) + 1;
		}
		String nextCouponId = String.format("PBLCPN_%03d", newCouponNum);
		adminCoupons.setPblcnCpnId(nextCouponId);
		adminCouponsMapper.addCoupon(adminCoupons);
	}

	@Override
	public Map<String, Object> getCouponsList(AdminCouponsSearch search) {
		int couponsCount = adminCouponsMapper.getCouponsCount(search);
		Pagination pagination = new Pagination(couponsCount, search);
		search.setOffset((search.getCurrentPage() - 1) * search.getPageSize());

		List<AdminCoupons> couponsList = adminCouponsMapper.getCouponsList(search);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("pagination", pagination);
		resultMap.put("couponsList", couponsList);
		resultMap.put("totalCount", couponsCount);

		return resultMap;
	}

	@Override
	public AdminCoupons getCouponById(String pblcnCpnId) {
		return adminCouponsMapper.getCouponById(pblcnCpnId);
	}

	@Override
	public void deleteCoupon(String pblcnCpnId) {
		adminCouponsMapper.deleteCoupon(pblcnCpnId);
	}

	 @Override
	    public void batchDeleteCoupons(List<String> pblcnCpnIdList) {
	        // [수정] 0 (비활성) 상태 값을 넘겨주도록 변경
	        adminCouponsMapper.batchUpdateActivationStatus(pblcnCpnIdList, 0);
	    }
	    
	    // [신규] 선택 활성화 메소드 구현
	    @Override
	    public void batchActivateCoupons(List<String> pblcnCpnIdList) {
	        // 1 (활성) 상태 값을 넘겨줌
	        adminCouponsMapper.batchUpdateActivationStatus(pblcnCpnIdList, 1);
	    }
}
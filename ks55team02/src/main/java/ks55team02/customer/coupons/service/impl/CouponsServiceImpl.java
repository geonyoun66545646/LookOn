package ks55team02.customer.coupons.service.impl;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.mapper.CouponsMapper;
import ks55team02.customer.coupons.service.CouponsService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponsServiceImpl implements CouponsService {

	private final CouponsMapper couponsMapper;
	private static final int PAGE_SIZE = 10;
	private static final int BLOCK_SIZE = 10;

	@Override
	public CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page) {
		// 1. 파라미터 조합
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userNo", userNo);
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);

		// 2. 전체 개수 조회
		int totalCount = couponsMapper.getAvailableCouponsCount(paramMap);

		// 3. 페이지네이션 정보 계산 및 파라미터 추가
		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		// 4. 해당 페이지 목록 조회
		List<Coupons> availableCoupons = couponsMapper.getAvailableCoupons(paramMap);

		// 5. 최종 CustomerPagination 객체 생성 및 반환
		return new CustomerPagination<>(availableCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	@Override
	public CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page) {
		// 위 getAvailableCoupons와 동일한 로직으로 보유 쿠폰 목록을 조회합니다.
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userNo", userNo);
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);

		int totalCount = couponsMapper.getMyCouponsCount(paramMap);

		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<UserCoupons> myCoupons = couponsMapper.getMyCoupons(paramMap);

		return new CustomerPagination<>(myCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	@Override
	@Transactional // 여러 DB 작업을 하므로 트랜잭션 처리가 중요합니다.
	public boolean issueCouponToUser(String userNo, String couponId) {

		// 1. 발급 전, 쿠폰의 정책 정보를 DB에서 미리 조회합니다.
		Coupons couponInfo = couponsMapper.getCouponInfo(couponId);
		if (couponInfo == null) {
			// 존재하지 않는 쿠폰이면 실패 처리 (혹은 예외 처리)
			return false;
		}

		// 2. 발급 조건(issue_condition_type)에 따라 자격 조건을 검사합니다.
		String condition = couponInfo.getIssueConditionType();

		if ("SIGN_UP".equals(condition)) {
			// 회원 가입일 당일 또는 특정 기간 내 발급만 허용할 수도 있음
			int count = couponsMapper.countUserCouponByCouponId(userNo, couponId);
			if (count > 0)
				return false;

		} else if ("FIRST_PURCHASE".equals(condition)) {
			int orderCount = couponsMapper.countUserOrders(userNo); // 주문 내역 있는지 확인
			if (orderCount > 0)
				return false;

		} else if ("REVIEW".equals(condition)) {
			int reviewCount = couponsMapper.countUserReviews(userNo);
			int monthlyIssuedCount = couponsMapper.countMonthlyIssuedReviewCoupons(userNo, couponId);
			if (reviewCount == 0 || monthlyIssuedCount >= 3)
				return false;

		} else if ("BIRTHDAY".equals(condition)) {
			boolean isBirthdayWeek = couponsMapper.isBirthdayWeek(userNo);
			if (!isBirthdayWeek)
				return false;

			int count = couponsMapper.countIssuedBirthdayCouponThisYear(userNo, couponId);
			if (count > 0)
				return false;

		} else if ("REISSUE_MONTHLY".equals(condition)) {
			int monthlyIssued = couponsMapper.countMonthlyIssuedCoupon(userNo, couponId);
			if (monthlyIssued > 0)
				return false;

		} else if ("MANUAL".equals(condition)) {
			// 제한 없음
		}

		// 3. 재발급 주기(reissue_cycle)에 따라 중복 여부를 검사합니다.
		String reissueCycle = couponInfo.getReissueCycle();
		int issuedCount = 0;

		if ("NONE".equals(reissueCycle)) {
		    issuedCount = couponsMapper.countUserCouponByCouponId(userNo, couponId);
		    if (issuedCount > 0) return false;

		} else if ("MONTHLY".equals(reissueCycle)) {
		    issuedCount = couponsMapper.countMonthlyIssuedCoupon(userNo, couponId);
		    if (issuedCount > 0) return false;

		} else if ("WEEKLY".equals(reissueCycle)) {
		    issuedCount = couponsMapper.countWeeklyIssuedCoupon(userNo, couponId);
		    if (issuedCount > 0) return false;

		} else if ("YEARLY".equals(reissueCycle)) {
		    issuedCount = couponsMapper.countYearlyIssuedCoupon(userNo, couponId);
		    if (issuedCount > 0) return false;
		}


		// 4. 모든 검사를 통과했다면, 쿠폰을 발급합니다.
		String nextUserCouponId = couponsMapper.getNextUserCouponId();
		UserCoupons newUserCoupon = new UserCoupons();
		newUserCoupon.setUserCpnId(nextUserCouponId);
		newUserCoupon.setUserNo(userNo);
		newUserCoupon.setPblcnCpnId(couponId);
		newUserCoupon.setIndivExpryDt(couponInfo.getVldEndDt());

		couponsMapper.issueUserCoupon(newUserCoupon);

		return true; // 최종 성공
	}
}
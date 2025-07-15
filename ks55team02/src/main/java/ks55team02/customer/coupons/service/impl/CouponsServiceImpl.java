package ks55team02.customer.coupons.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.mapper.CouponsMapper;
import ks55team02.customer.coupons.service.CouponsService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CouponsServiceImpl implements CouponsService {

	private final CouponsMapper couponsMapper;
	private static final int PAGE_SIZE = 10;
	private static final int BLOCK_SIZE = 10;

	// (수정) getAvailableCoupons 메소드가 userNo를 받도록 변경합니다.
	@Override
	public CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);

		int totalCount = couponsMapper.getAvailableCouponsCount(paramMap);

		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<Coupons> availableCoupons = couponsMapper.getAvailableCoupons(paramMap);

		// (핵심 로직 추가) 사용자가 로그인한 경우, 각 쿠폰의 보유 여부를 체크합니다.
		if (userNo != null && !userNo.trim().isEmpty()) {
			// 1. 사용자가 보유한 모든 쿠폰의 ID 목록을 한 번에 조회합니다. (성능 최적화)
			List<String> ownedCouponIds = couponsMapper.getOwnedCouponIds(userNo);

			// 2. 조회된 쿠폰 목록을 순회하며, 보유 여부(isOwned)를 설정합니다.
			// for (Coupons coupon : availableCoupons) 루프 내부
			for (Coupons coupon : availableCoupons) {
				if (ownedCouponIds.contains(coupon.getPblcnCpnId())) {
					// (수정) setOwned(true)로 호출합니다.
					coupon.setOwned(true);
				}
			}
		}
		return new CustomerPagination<>(availableCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	@Override
	public CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page,
			Boolean isUsed) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userNo", userNo);
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);
		// (개선) isUsed 파라미터를 추가하여 사용 가능/완료 쿠폰 필터링
		if (isUsed != null) {
			paramMap.put("isUsed", isUsed);
		}

		int totalCount = couponsMapper.getMyCouponsCount(paramMap);

		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<UserCoupons> myCoupons = couponsMapper.getMyCoupons(paramMap);

		return new CustomerPagination<>(myCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	@Override
	@Transactional
	public Map<String, Object> issueCouponToUser(String userNo, String couponId) {
		Map<String, Object> result = new HashMap<>();

		Coupons couponInfo = couponsMapper.getCouponInfo(couponId);
		if (couponInfo == null) {
			result.put("success", false);
			result.put("message", "존재하지 않는 쿠폰입니다.");
			return result;
		}
		// (수정) isIssuable은 이제 실패 이유(String)를 반환합니다. 성공 시 null을 반환합니다.
		String failureReason = isIssuable(userNo, couponInfo);

		if (failureReason != null) {
			// 발급 조건 검증 실패
			log.warn("쿠폰 발급 거절: {}. (userNo: {}, couponId: {})", failureReason, userNo, couponId);
			result.put("success", false);
			result.put("message", failureReason);
			return result;
		}

		// 모든 검증 통과 후 쿠폰 발급
		issueNewCoupon(userNo, couponInfo);
		log.info("쿠폰 발급 성공! (userNo: {}, couponId: {})", userNo, couponId);
		result.put("success", true);
		result.put("message", "쿠폰이 발급되었습니다.");
		return result;
	}

	/**
	 * (완벽 복구) '두 단계 검증' 방식으로 모든 상세 발급 조건을 검사합니다.
	 * 
	 * @return 발급 실패 이유(String). 발급 가능 시 null을 반환합니다.
	 */
	private String isIssuable(String userNo, Coupons couponInfo) {
		String couponId = couponInfo.getPblcnCpnId();

		// --- 1단계: 공통 검증 (가장 먼저!) ---
		int issueCount = couponsMapper.countUserCouponByCouponId(userNo, couponId);
		if (issueCount > 0) {
			return "이미 발급받은 쿠폰입니다.";
		}

		// --- 2단계: 특별 조건 검증 ---
		String conditionType = couponInfo.getIssueConditionType();
		if (conditionType == null || "ANYONE".equals(conditionType)) {
			return null; // 조건 없으면 통과
		}

		switch (conditionType) {
		case "SIGN_UP":
			return null; // 1단계 통과했으면 OK

		case "FIRST_PURCHASE":
			if (couponsMapper.countUserOrders(userNo) > 0) {
				return "발급 조건이 맞지 않습니다. (사유: 첫 구매 전용)";
			}
			break;

		case "REVIEW":
			if (couponsMapper.countUserReviews(userNo) == 0) {
				return "발급 조건이 맞지 않습니다. (사유: 리뷰 작성 필요)";
			}
			break;

		case "BIRTHDAY":
			if (!couponsMapper.isBirthdayWeek(userNo)) {
				return "발급 조건이 맞지 않습니다. (사유: 생일 주간이 아님)";
			}
			// 생일 쿠폰은 1년에 한 번만 받을 수 있다는 추가 규칙 적용
			if (couponsMapper.countIssuedBirthdayCouponThisYear(userNo, couponId) > 0) {
				return "올해의 생일 쿠폰은 이미 발급받았습니다.";
			}
			break;
		}

		return null; // 모든 특별 조건을 통과했으면 최종 성공
	}

	/**
	 * 실제 쿠폰을 발급(INSERT)하는 로직만 담당합니다. (수정 없음)
	 */
	private void issueNewCoupon(String userNo, Coupons couponInfo) {
		String nextUserCouponId = couponsMapper.getNextUserCouponId();
		UserCoupons newUserCoupon = new UserCoupons();
		newUserCoupon.setUserCpnId(nextUserCouponId);
		newUserCoupon.setUserNo(userNo);
		newUserCoupon.setPblcnCpnId(couponInfo.getPblcnCpnId());
		newUserCoupon.setIndivExpryDt(couponInfo.getVldEndDt());
		// 발급 사유는 쿠폰의 발급 조건 타입을 따라가는 것이 일관성 있어 보입니다.
		newUserCoupon.setIssuRsnSrcCn(couponInfo.getIssueConditionType());

		couponsMapper.issueUserCoupon(newUserCoupon);
	}

	@Override
	public List<UserCoupons> getUserAvailableCoupons(String userNo) {
		if (userNo == null || userNo.trim().isEmpty()) {
			throw new IllegalArgumentException("사용자 번호가 유효하지 않습니다.");
		}
		// (개선) Mapper에서 이미 완벽하게 필터링하므로, 서비스단의 추가 필터링 로직은 제거합니다.
		return couponsMapper.getUserAvailableCoupons(userNo);
	}

	/*
	 * ─── 삭제된 메소드 ──────────────────────────────────────────────────────────
	 * private Map<String, Object> checkCouponStatusForUser(...) ->
	 * isIssuable(userNo, couponId) boolean 메소드로 완전 대체됨.
	 * ────────────────────────────────────────────────────────────────────────
	 */
}
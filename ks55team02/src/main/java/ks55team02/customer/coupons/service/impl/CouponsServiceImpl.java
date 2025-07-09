package ks55team02.customer.coupons.service.impl;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.mapper.CouponsMapper;
import ks55team02.customer.coupons.service.CouponsService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CouponsServiceImpl implements CouponsService {

	private final CouponsMapper couponsMapper;
	private static final int PAGE_SIZE = 10;
	private static final int BLOCK_SIZE = 10;

	@Override
	public CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userNo", userNo);
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);

		int totalCount = couponsMapper.getAvailableCouponsCount(paramMap);

		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<Coupons> availableCoupons = couponsMapper.getAvailableCoupons(paramMap);

		if (userNo != null && !userNo.isEmpty()) { // 로그인한 사용자에게만 발급 가능 여부를 체크
			for (Coupons coupon : availableCoupons) {
				Map<String, Object> status = checkCouponStatusForUser(userNo, coupon);
				coupon.setIssuable((Boolean) status.get("isIssuable"));
				coupon.setStatusMessage((String) status.get("statusMessage"));
			}
		} else { // 비로그인 사용자에게는 모든 쿠폰을 발급 불가능으로 표시
			for (Coupons coupon : availableCoupons) {
				coupon.setIssuable(false);
				coupon.setStatusMessage("로그인 후 확인 가능"); // 비로그인 시 메시지
			}
		}

		return new CustomerPagination<>(availableCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
	}

	@Override
	public CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page) {
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
	@Transactional
	public boolean issueCouponToUser(String userNo, String couponId) {
		Coupons couponInfo = couponsMapper.getCouponInfo(couponId);
		if (couponInfo == null) {
			throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
		}

		// 1. 발급 자격 검사 (여기서 checkCouponStatusForUser 호출)
		Map<String, Object> eligibilityResult = checkCouponStatusForUser(userNo, couponInfo);
		boolean isEligible = (Boolean) eligibilityResult.get("isIssuable");
		String statusMessage = (String) eligibilityResult.get("statusMessage");

		if (!isEligible) {
			// 발급 불가능한 경우, 해당 메시지를 컨트롤러로 전달하기 위해 예외 발생
			throw new IllegalArgumentException(statusMessage != null ? statusMessage : "쿠폰 발급 조건을 충족하지 않습니다.");
		}

		// 2. 모든 검사를 통과했으면 실제 쿠폰 발급
		issueNewCoupon(userNo, couponInfo);

		return true; // 최종 성공
	}

	/**
	 * 쿠폰 발급 자격 조건을 검사하고, 결과를 Map으로 반환합니다.
	 * isIssuable (boolean)과 statusMessage (String)를 포함합니다.
	 * * @param userNo 사용자 번호
	 * @param couponInfo 검사할 쿠폰 정보
	 * @return isIssuable (boolean)과 statusMessage (String)를 포함하는 Map
	 */
	private Map<String, Object> checkCouponStatusForUser(String userNo, Coupons couponInfo) {
		Map<String, Object> result = new HashMap<>();
		result.put("isIssuable", true); // 기본은 발급 가능
		result.put("statusMessage", null); // 기본 메시지 없음

		String condition = couponInfo.getIssueConditionType();
		String reissueCycle = couponInfo.getReissueCycle();
		String pblcnCpnId = couponInfo.getPblcnCpnId();

		log.info("--- 쿠폰 발급 가능 여부 검사 시작 (userNo: {}, couponId: {}) ---", userNo, pblcnCpnId);
		log.info("쿠폰 타입: {}, 재발급 주기: {}", condition, reissueCycle);

		// ★★★ 1. UNIVERSAL: 이 사용자가 이 공개 쿠폰을 이미 '활성 상태'로 보유하고 있는가? ★★★
		// 이 체크는 모든 쿠폰 타입에 대해 가장 먼저 수행되어야 합니다.
		// 사용자가 이미 활성 상태의 쿠폰을 보유하고 있다면, 더 이상 발급 가능하지 않습니다.
		int activeOwnedCount = couponsMapper.countActiveUserCouponByCouponId(userNo, pblcnCpnId);
		log.info("UNIVERSAL 보유 검사 - 활성 보유 이력: {}개", activeOwnedCount);
		if (activeOwnedCount > 0) {
			result.put("isIssuable", false);
			result.put("statusMessage", "이미 발급받았습니다."); // "보유 쿠폰"을 나타내는 메시지 (프론트엔드 판단 기준)
			log.info("UNIVERSAL 보유 - 이미 발급받음: isIssuable = false");
			return result; // 이미 보유했으면 더 이상 조건 검사할 필요 없이 반환
		}

		// ★★★ 2. 발급 조건(issue_condition_type)에 따른 상세 검사 ★★★
		// (여기에 도달했다는 것은 현재 활성 쿠폰을 보유하고 있지 않다는 의미)

		if ("SIGN_UP".equals(condition)) {
			// SIGN_UP은 단 1회성이므로, 위 UNIVERSAL 체크로 이미 충분합니다.
			// (활성 쿠폰을 보유하고 있지 않으므로, 이전에 발급받은 적이 없다는 의미)
		} else if ("FIRST_PURCHASE".equals(condition)) {
			int orderCount = couponsMapper.countUserOrders(userNo);
			log.info("FIRST_PURCHASE 검사 - 주문 이력: {}개", orderCount);
			if (orderCount > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "이미 구매 이력이 있습니다.");
				log.info("FIRST_PURCHASE - 구매 이력 있음: isIssuable = false");
				return result;
			}
		} else if ("REVIEW".equals(condition)) {
			// REVIEW는 월 3회 제한이 있으므로, 이 부분에서 추가 검사.
			int reviewCount = couponsMapper.countUserReviews(userNo);
			log.info("REVIEW 검사 - 총 리뷰 개수: {}개", reviewCount);
			if (reviewCount == 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "리뷰 작성 내역이 없습니다.");
				log.info("REVIEW - 리뷰 없음: isIssuable = false");
				return result;
			}
            // 금월 발급 이력 확인 (월 3회 제한)
			int monthlyReviewCouponCount = couponsMapper.countMonthlyIssuedReviewCoupons(userNo, pblcnCpnId);
			log.info("REVIEW 검사 - 금월 발급 리뷰 쿠폰: {}개", monthlyReviewCouponCount);
			if (monthlyReviewCouponCount >= 3) { // 월 3회 제한
				result.put("isIssuable", false);
				result.put("statusMessage", "금월 발급이 완료되었습니다. (월 3회 제한)");
				log.info("REVIEW - 월 3회 초과: isIssuable = false");
				return result;
			}
		} else if ("BIRTHDAY".equals(condition)) {
			boolean isBirthdayWeek = couponsMapper.isBirthdayWeek(userNo);
			log.info("BIRTHDAY 검사 - 생일 주간 여부: {}", isBirthdayWeek);
			if (!isBirthdayWeek) {
				result.put("isIssuable", false);
				result.put("statusMessage", "생일 주간이 아닙니다.");
				log.info("BIRTHDAY - 생일 주간 아님: isIssuable = false");
				return result;
			}
            // 올해 발급 이력 확인 (연 1회 제한)
			int yearlyBirthdayCouponCount = couponsMapper.countIssuedBirthdayCouponThisYear(userNo, pblcnCpnId);
			log.info("BIRTHDAY 검사 - 올해 발급 생일 쿠폰: {}개", yearlyBirthdayCouponCount);
			if (yearlyBirthdayCouponCount > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "올해 생일 쿠폰은 이미 발급받았습니다.");
				log.info("BIRTHDAY - 올해 이미 발급: isIssuable = false");
				return result;
			}
		} else if ("REISSUE_MONTHLY".equals(condition)) {
			// REISSUE_MONTHLY는 월별 재발급이므로, 금월 발급 이력을 확인합니다.
			int monthlyIssuedCount = couponsMapper.countMonthlyIssuedCoupon(userNo, pblcnCpnId);
			log.info("REISSUE_MONTHLY 검사 - 금월 발급 쿠폰: {}개", monthlyIssuedCount);
			if (monthlyIssuedCount > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "금월 발급이 완료되었습니다.");
				log.info("REISSUE_MONTHLY - 금월 이미 발급: isIssuable = false");
				return result;
			}
		}
		// 'UNLIMITED' 재발급 주기는 별도의 발급 조건이 없다면,
		// 위 UNIVERSAL 체크에서 이미 활성 보유 여부를 판단하므로 추가 로직이 필요 없습니다.

		log.info("--- 쿠폰 발급 가능 여부 검사 완료 (isIssuable: {}, statusMessage: {}) ---", result.get("isIssuable"), result.get("statusMessage"));
		return result;
	}

	/**
	 * 실제 쿠폰을 발급(INSERT)하는 로직만 담당합니다.
	 */
	private void issueNewCoupon(String userNo, Coupons couponInfo) {
		String nextUserCouponId = couponsMapper.getNextUserCouponId();
		UserCoupons newUserCoupon = new UserCoupons();
		newUserCoupon.setUserCpnId(nextUserCouponId);
		newUserCoupon.setUserNo(userNo);
		newUserCoupon.setPblcnCpnId(couponInfo.getPblcnCpnId());
		newUserCoupon.setIndivExpryDt(couponInfo.getVldEndDt());
		newUserCoupon.setIssuRsnSrcCn(couponInfo.getIssueConditionType());

		couponsMapper.issueUserCoupon(newUserCoupon);
	}
}
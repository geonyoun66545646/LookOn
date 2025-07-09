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

import java.time.LocalDate; // 필요하다면 추가 (날짜 계산 시)
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
		// 1. 파라미터 조합
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("userNo", userNo); // 사용자 번호도 맵에 추가
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

		// ★★★ 5. 각 쿠폰의 발급 가능 여부 (isIssuable) 및 상태 메시지 (statusMessage) 설정 로직 추가 ★★★
		if (userNo != null && !userNo.isEmpty()) { // 로그인한 사용자에게만 발급 가능 여부를 체크
			for (Coupons coupon : availableCoupons) {
				Map<String, Object> status = checkCouponStatusForUser(userNo, coupon);
				coupon.setIssuable((Boolean) status.get("isIssuable"));
				coupon.setStatusMessage((String) status.get("statusMessage"));
			}
		} else { // 비로그인 사용자에게는 모든 쿠폰을 발급 불가능으로 표시 (또는 다른 정책)
			for (Coupons coupon : availableCoupons) {
				coupon.setIssuable(false);
				coupon.setStatusMessage("로그인 후 확인 가능"); // 비로그인 시 메시지
			}
		}

		// 6. 최종 CustomerPagination 객체 생성 및 반환
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

	// =======================================================
	// ▼▼▼ 기존 issueCouponToUser 메서드를 아래와 같이 수정합니다. ▼▼▼
	// =======================================================

	/**
	 * [수정된 public 메서드] 쿠폰 발급의 전체 흐름을 관리합니다. 이 메서드는 예외를 던지도록 수정하여 컨트롤러에서 구체적인 메시지를
	 * 반환할 수 있도록 합니다.
	 *
	 * @throws IllegalArgumentException 쿠폰 발급 조건이 충족되지 않을 경우 발생
	 */
	@Override
	@Transactional
	public boolean issueCouponToUser(String userNo, String couponId) {
		Coupons couponInfo = couponsMapper.getCouponInfo(couponId);
		if (couponInfo == null) {
			throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
		}

		// 1. 발급 자격 검사 (여기서는 바로 예외를 던지도록 수정)
		Map<String, Object> eligibilityResult = checkCouponStatusForUser(userNo, couponInfo);
		boolean isEligible = (Boolean) eligibilityResult.get("isIssuable");
		String statusMessage = (String) eligibilityResult.get("statusMessage");

		if (!isEligible) {
			throw new IllegalArgumentException(statusMessage != null ? statusMessage : "쿠폰 발급 조건을 충족하지 않습니다.");
		}

		// 2. 모든 검사를 통과했으면 실제 쿠폰 발급
		issueNewCoupon(userNo, couponInfo);

		return true; // 최종 성공
	}

	/**
	 * [새로운 private 메서드 1] 쿠폰 발급 자격 조건을 검사하고, 결과를 Map으로 반환합니다. isIssuable (boolean)과
	 * statusMessage (String)를 포함합니다.
	 */
	private Map<String, Object> checkCouponStatusForUser(String userNo, Coupons couponInfo) {
		Map<String, Object> result = new HashMap<>();
		result.put("isIssuable", true); // 기본은 발급 가능
		result.put("statusMessage", null); // 기본 메시지 없음

		String condition = couponInfo.getIssueConditionType();
		String reissueCycle = couponInfo.getReissueCycle();

		// ★★★ 여기에 첫 번째 로그를 넣어주세요. (메서드 시작 시점) ★★★
		log.info("--- 쿠폰 발급 가능 여부 검사 시작 (userNo: {}, couponId: {}) ---", userNo, couponInfo.getPblcnCpnId());
		log.info("쿠폰 타입: {}, 재발급 주기: {}", condition, reissueCycle);

		// 1. 발급 조건(issue_condition_type) 검사
		if ("SIGN_UP".equals(condition)) {
			int count = couponsMapper.countUserCouponByCouponId(userNo, couponInfo.getPblcnCpnId());
			log.info("SIGN_UP 검사 - 발급 이력: {}개", count); // ★★★ 각 조건 검사 바로 아래에 관련 로그를 넣어주세요. ★★★
			if (count > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "이미 발급받았습니다.");
				log.info("SIGN_UP - 이미 발급받음: isIssuable = false"); // ★★★ 결과 반환 직전 로그 ★★★
				return result;
			}
		} else if ("FIRST_PURCHASE".equals(condition)) {
			int count = couponsMapper.countUserOrders(userNo);
			log.info("FIRST_PURCHASE 검사 - 주문 이력: {}개", count); // ★★★
			if (count > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "이미 구매 이력이 있습니다.");
				log.info("FIRST_PURCHASE - 구매 이력 있음: isIssuable = false"); // ★★★
				return result;
			}
		} else if ("REVIEW".equals(condition)) {
			int reviewCount = couponsMapper.countUserReviews(userNo);
			log.info("REVIEW 검사 - 총 리뷰 개수: {}개", reviewCount); // ★★★
			if (reviewCount == 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "리뷰 작성 내역이 없습니다.");
				log.info("REVIEW - 리뷰 없음: isIssuable = false"); // ★★★
				return result;
			}
			int monthlyReviewCouponCount = couponsMapper.countMonthlyIssuedReviewCoupons(userNo,
					couponInfo.getPblcnCpnId());
			log.info("REVIEW 검사 - 금월 발급 리뷰 쿠폰: {}개", monthlyReviewCouponCount); // ★★★
			if (monthlyReviewCouponCount >= 3) {
				result.put("isIssuable", false);
				result.put("statusMessage", "금월 발급이 완료되었습니다. (월 3회 제한)");
				log.info("REVIEW - 월 3회 초과: isIssuable = false"); // ★★★
				return result;
			}
		} else if ("BIRTHDAY".equals(condition)) {
			boolean isBirthdayWeek = couponsMapper.isBirthdayWeek(userNo);
			log.info("BIRTHDAY 검사 - 생일 주간 여부: {}", isBirthdayWeek); // ★★★
			if (!isBirthdayWeek) {
				result.put("isIssuable", false);
				result.put("statusMessage", "생일 주간이 아닙니다.");
				log.info("BIRTHDAY - 생일 주간 아님: isIssuable = false"); // ★★★
				return result;
			}
			int yearlyBirthdayCouponCount = couponsMapper.countIssuedBirthdayCouponThisYear(userNo,
					couponInfo.getPblcnCpnId());
			log.info("BIRTHDAY 검사 - 올해 발급 생일 쿠폰: {}개", yearlyBirthdayCouponCount); // ★★★
			if (yearlyBirthdayCouponCount > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "올해 생일 쿠폰은 이미 발급받았습니다.");
				log.info("BIRTHDAY - 올해 이미 발급: isIssuable = false"); // ★★★
				return result;
			}
		} else if ("REISSUE_MONTHLY".equals(condition)) {
			int monthlyCouponCount = couponsMapper.countMonthlyIssuedCoupon(userNo, couponInfo.getPblcnCpnId());
			log.info("REISSUE_MONTHLY 검사 - 금월 발급 쿠폰: {}개", monthlyCouponCount); // ★★★
			if (monthlyCouponCount > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "금월 발급이 완료되었습니다.");
				log.info("REISSUE_MONTHLY - 금월 이미 발급: isIssuable = false"); // ★★★
				return result;
			}
		}
		// 'NONE' 재발급 주기 검사
		if ("NONE".equals(reissueCycle)) {
			int count = couponsMapper.countUserCouponByCouponId(userNo, couponInfo.getPblcnCpnId());
			log.info("재발급 주기 NONE 검사 - 발급 이력: {}개", count); // ★★★
			if (count > 0) {
				result.put("isIssuable", false);
				result.put("statusMessage", "이미 발급받았습니다.");
				log.info("NONE - 이미 발급받음: isIssuable = false"); // ★★★
				return result;
			}
		}

		// ★★★ 여기에 마지막 로그를 넣어주세요. (메서드 끝, return 직전) ★★★
		log.info("--- 쿠폰 발급 가능 여부 검사 완료 (isIssuable: {}, statusMessage: {}) ---", result.get("isIssuable"),
				result.get("statusMessage"));
		return result; // 모든 검사를 통과하면 { isIssuable: true, statusMessage: null } 반환
	}

	/**
	 * [새로운 private 메서드 2] 실제 쿠폰을 발급(INSERT)하는 로직만 담당합니다.
	 */
	private void issueNewCoupon(String userNo, Coupons couponInfo) {
		String nextUserCouponId = couponsMapper.getNextUserCouponId();
		UserCoupons newUserCoupon = new UserCoupons();
		newUserCoupon.setUserCpnId(nextUserCouponId);
		newUserCoupon.setUserNo(userNo);
		newUserCoupon.setPblcnCpnId(couponInfo.getPblcnCpnId());
		newUserCoupon.setIndivExpryDt(couponInfo.getVldEndDt());
		// issue_condition_type 값을 issu_rsn_src_cn 에 삽입 (논의했던 부분)
		newUserCoupon.setIssuRsnSrcCn(couponInfo.getIssueConditionType());

		couponsMapper.issueUserCoupon(newUserCoupon);
	}
}
package ks55team02.customer.coupons.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	// CouponsServiceImpl.java
	@Override
	public CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("keyword", keyword);
		paramMap.put("sortOrder", sortOrder);
		paramMap.put("userNo", userNo); // ⭐⭐ userNo를 paramMap에 추가 ⭐⭐

		int totalCount = couponsMapper.getAvailableCouponsCount(paramMap); // ⭐ 이제 이 쿼리가 보유 쿠폰 제외한 개수를 반환

		int offset = (page - 1) * PAGE_SIZE;
		paramMap.put("limit", PAGE_SIZE);
		paramMap.put("offset", offset);

		List<Coupons> availableCoupons = couponsMapper.getAvailableCoupons(paramMap); // ⭐ 이제 이 쿼리가 보유 쿠폰 제외한 리스트를 반환

		// ⭐⭐ 이 for 루프 (혹은 stream 필터링)는 이제 필요 없습니다. ⭐⭐
		// 매퍼에서 이미 제외되었기 때문입니다.
		// if (userNo != null && !userNo.trim().isEmpty()) {
		// List<String> ownedCouponIds = couponsMapper.getOwnedCouponIds(userNo);
		// availableCoupons = availableCoupons.stream()
		// .filter(coupon -> !ownedCouponIds.contains(coupon.getPblcnCpnId()))
		// .collect(Collectors.toList());
		// }

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

	// * - 2025.07.11 gy -
	@Override
	public List<UserCoupons> getUserAvailableCoupons(String userNo) {
		if (userNo == null || userNo.trim().isEmpty()) {
			throw new IllegalArgumentException("사용자 번호가 유효하지 않습니다.");
		}
		// (개선) Mapper에서 이미 완벽하게 필터링하므로, 서비스단의 추가 필터링 로직은 제거합니다.
		return couponsMapper.getUserAvailableCoupons(userNo);
	}

	/*
	 **
	 * 
	 * - 2025.07.11 gy - 특정 사용자가 보유한 사용 가능한 쿠폰 목록을 조회합니다. 이 메서드는 CouponMapper를 호출하여
	 * DB에서 데이터를 가져옵니다.
	 *
	 * @param userNo 사용자 번호
	 * 
	 * @return 사용 가능한 UserCoupons 객체 리스트
	 * 
	 * @Override public List<UserCoupons> getUserAvailableCoupons(String userNo) {
	 * // 1. 파라미터 검증 if (userNo == null || userNo.trim().isEmpty()) { throw new
	 * IllegalArgumentException("사용자 번호가 유효하지 않습니다."); }
	 * 
	 * // 2. 쿠폰 조회 log.debug("사용자 보유 쿠폰 조회 시작 - userNo: {}", userNo);
	 * List<UserCoupons> coupons = couponsMapper.getUserAvailableCoupons(userNo); //
	 * 여기를 확인해주세요!
	 * 
	 * // 3. 결과 검증 및 추가 필터링 (필요시) - 예: 만료일이 지난 쿠폰 제외 List<UserCoupons> validCoupons
	 * = coupons.stream() .filter(c -> c.getIndivExpryDt() == null ||
	 * !c.getIndivExpryDt().toLocalDate().isBefore(LocalDate.now())) // 만료일이 현재보다
	 * 이전이 아닌 쿠폰만 필터링 .collect(Collectors.toList());
	 * 
	 * log.debug("사용자 보유 쿠폰 조회 완료 - 유효 쿠폰 수: {}", validCoupons.size()); return
	 * validCoupons; }
	 */

	/*
	 * ─── 삭제된 메소드 ──────────────────────────────────────────────────────────
	 * private Map<String, Object> checkCouponStatusForUser(...) ->
	 * isIssuable(userNo, couponId) boolean 메소드로 완전 대체됨.
	 * ────────────────────────────────────────────────────────────────────────
	 */
}
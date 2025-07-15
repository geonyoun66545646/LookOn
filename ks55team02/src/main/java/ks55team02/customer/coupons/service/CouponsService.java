package ks55team02.customer.coupons.service;

import java.util.List;
import java.util.Map;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.util.CustomerPagination;

// @Service 어노테이션은 구현 클래스(CouponsServiceImpl)에 붙이는 것이 일반적입니다.
// 인터페이스에는 보통 붙이지 않습니다.
public interface CouponsService {

	/**
	 * 모든 사용자가 다운로드할 수 있는, 현재 발급 가능한 쿠폰 목록과 페이지 정보를 조회합니다.
	 * 
	 * @param keyword   검색어
	 * @param sortOrder 정렬 순서
	 * @param page      요청 페이지 번호
	 * @return 페이지네이션이 적용된 발급 가능 쿠폰 목록
	 */
	CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page);

	/**
	 * 특정 사용자가 보유한 쿠폰 목록과 페이지 정보를 조회합니다. isUsed 파라미터를 통해 사용 가능/사용 완료된 쿠폰을 필터링할 수
	 * 있습니다.
	 * 
	 * @param userNo    현재 로그인한 사용자 아이디
	 * @param keyword   검색어
	 * @param sortOrder 정렬 순서
	 * @param page      요청 페이지 번호
	 * @param isUsed    조회할 쿠폰 상태 (null: 전체, false: 사용 가능, true: 사용 완료)
	 * @return 페이지네이션이 적용된 보유 쿠폰 목록
	 */
	CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page,
			Boolean isUsed);

	/**
	 * 사용자에게 쿠폰을 발급합니다. 내부적으로 재발급 불가 정책에 따라 중복 발급 여부를 검증합니다.
	 * 
	 * @param userNo   쿠폰을 발급받을 사용자 아이디
	 * @param couponId 발급받을 쿠폰의 ID
	 * @return 쿠폰 발급 성공 시 true, 실패(중복 발급 등) 시 false
	 */
	Map<String, Object> issueCouponToUser(String userNo, String couponId);
	

	/**
	 * - 2025.07.11 gy -
	 * 결제 등 특정 상황에서, 사용자가 '현재 시점'에 사용 가능한 쿠폰 목록을 조회합니다. (기간 만료, 사용 완료 쿠폰 제외)
	 * 
	 * @param userNo 사용자 번호
	 * @return 사용 가능한 UserCoupons 객체 리스트
	 */
	List<UserCoupons> getUserAvailableCoupons(String userNo);

}
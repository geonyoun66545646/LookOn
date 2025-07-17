package ks55team02.customer.coupons.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;

@Mapper
public interface CouponsMapper {

    /**
     * 발급 가능한 쿠폰 목록 조회
     */
    List<Coupons> getAvailableCoupons(Map<String, Object> paramMap);

    /**
     * 발급 가능한 쿠폰의 전체 개수 조회
     */
    int getAvailableCouponsCount(Map<String, Object> paramMap);

    /**
     * 보유한 쿠폰 목록 조회
     */
    List<UserCoupons> getMyCoupons(Map<String, Object> paramMap);

    /**
     * 보유한 쿠폰의 전체 개수 조회
     */
    int getMyCouponsCount(Map<String, Object> paramMap);
    
    /**
     * 특정 쿠폰의 정보를 조회 (쿠폰 발급 전 조건 확인용)
     */
    Coupons getCouponInfo(String couponId);
    
    /**
     * 사용자가 특정 쿠폰을 이미 보유하고 있는지 확인
     */
    int countUserCouponByCouponId(String userNo, String couponId);

    /**
     * 사용자에게 쿠폰 발급 (user_coupons 테이블에 INSERT)
     */
    void issueUserCoupon(UserCoupons userCoupon);
    
    
    /**
     * [추가] 사용 완료된 쿠폰을 재발급 처리 (상태 갱신)
     */
    int reissueUserCoupon(UserCoupons userCoupon);
    
    /**
     * 새로운 사용자 쿠폰 ID를 생성하기 위한 쿼리
     * @return 생성될 다음 사용자 쿠폰 ID (예: USRCPN_055)
     */
    String getNextUserCouponId();
    
    /**
     * 사용자의 주문 횟수를 조회 (FIRST_PURCHASE 조건 확인용)
     */
    int countUserOrders(String userNo);

    /**
     * 사용자의 리뷰 작성 횟수를 조회 (REVIEW 조건 확인용)
     */
    int countUserReviews(String userNo);

    /**
     * 해당 쿠폰을 사용자가 이번 달에 리뷰 쿠폰으로 몇 번 발급받았는지 확인 (REVIEW 월 3회 제한용)
     */
    int countMonthlyIssuedReviewCoupons(String userNo, String couponId);

    /**
     * 사용자가 생일 주간에 해당하는지 확인 (BIRTHDAY 조건 확인용)
     */
    boolean isBirthdayWeek(String userNo);

    /**
     * 사용자가 올해 생일 쿠폰을 이미 발급받았는지 확인 (BIRTHDAY 연 1회 제한)
     */
    int countIssuedBirthdayCouponThisYear(String userNo, String couponId);

    /**
     * 사용자가 해당 쿠폰을 이번 달에 발급받은 횟수 확인 (MONTHLY 재발급 조건 확인)
     */
    int countMonthlyIssuedCoupon(String userNo, String couponId);

    /**
     * 사용자가 해당 쿠폰을 이번 주에 발급받은 횟수 확인 (WEEKLY 재발급 조건 확인)
     */
    int countWeeklyIssuedCoupon(String userNo, String couponId);

    /**
     * 사용자가 해당 쿠폰을 올해 발급받은 횟수 확인 (YEARLY 재발급 조건 확인)
     */
    int countYearlyIssuedCoupon(String userNo, String couponId);

    /**
     * 특정 사용자가 특정 공개 쿠폰을 현재 '활성 상태' (사용되지 않았거나 만료되지 않은)로 보유하고 있는지 개수를 조회합니다.
     * @param userNo 사용자 번호
     * @param pblcnCpnId 공개 쿠폰 ID
     * @return 활성 상태로 보유 중인 쿠폰 개수 (0 또는 1)
     */
    int countActiveUserCouponByCouponId(@Param("userNo") String userNo, @Param("pblcnCpnId") String pblcnCpnId);
    
    /**
     * 사용 가능한 사용자 쿠폰 목록 조회
     * (2025.07.11 gy - checkOut.html에서 사용)
     */
    List<UserCoupons> getUserAvailableCoupons(String userNo);
    
    List<String> getOwnedCouponIds(String userNo);
    

}